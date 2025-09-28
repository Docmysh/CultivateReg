package com.bo.cultivatereg.client.gui;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.manual.CultivationManual;
import com.bo.cultivatereg.network.ManualQuizCompletePacket;
import com.bo.cultivatereg.network.Net;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Study interface that renders the manual across two book pages and runs the comprehension quiz at the end.
 */
@OnlyIn(Dist.CLIENT)
public class ManualStudyScreen extends Screen {
    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(CultivateReg.MODID, "textures/gui/manual_book.png");
    private static final Component PAGE_BREAK = Component.literal("FORCE_PAGE_BREAK");

    // --- CORRECTED CONSTANTS ---
    private static final int BOOK_WIDTH = 258;
    private static final int BOOK_HEIGHT = 192;
    // Reduce the text width to fit within one page and leave a gap for the spine.
    private static final int PAGE_TEXT_WIDTH = 100;
    private static final int PAGE_TEXT_HEIGHT = 130;
    private static final int PAGE_BOTTOM_MARGIN = 8;
    // Adjust X coordinates for a two-page layout within the 192px book width.
    private static final int LEFT_PAGE_X = 24; // Margin from the left edge of the book
    private static final int RIGHT_PAGE_X = BOOK_WIDTH / 2 + 10; // Start after the center spine
    private static final int PAGE_TEXT_Y = 28; // Lowered to make space for a multi-line title
    // --- END OF CORRECTIONS ---


    private static final int BODY_TEXT_COLOR = 0x3F3F3F;
    private static final int INSTRUCTION_TEXT_COLOR = 0x473820;
    private static final int PAGE_HINT_COLOR = 0x5A4E3A;
    private static final int SECTION_HEADING_COLOR = 0xBD8A3C;
    private static final int FINAL_PROMPT_COLOR = 0x2F6A32;
    private static final int PAGE_INDICATOR_COLOR = 0x5A4E3A;
    private static final int QUIZ_HEADING_COLOR = 0x5E3A1C;
    private static final int QUIZ_TEXT_COLOR = 0x473820;

    private final CultivationManual manual;
    private final ItemStack manualStack;

    private final List<List<PageLine>> pages = new ArrayList<>();
    private final List<Button> optionButtons = new ArrayList<>();

    private PageButton previousPageButton;
    private PageButton nextPageButton;
    private Button beginButton;

    private int spreadIndex = 0;
    private int questionIndex = -1;
    private boolean completionSent = false;
    private boolean alreadyMastered = false;
    private MutableComponent feedback = Component.empty().copy();

    public ManualStudyScreen(CultivationManual manual, ItemStack stack) {
        super(Component.literal(manual.displayName()));
        this.manual = manual;
        this.manualStack = stack.copy();
    }

    public static void open(CultivationManual manual, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        mc.setScreen(new ManualStudyScreen(manual, stack));
    }

    @Override
    protected void init() {
        super.init();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                alreadyMastered = data.isManualQuizPassed() && manual.id().equals(data.getManualId());
                if (alreadyMastered) {
                    completionSent = true;
                }
            });
        }

        previousPageButton = this.addRenderableWidget(new PageButton(0, 0, false, this::previousSpread));
        nextPageButton = this.addRenderableWidget(new PageButton(0, 0, true, this::nextSpread));

        beginButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.cultivatereg.manual.begin"),
                        b -> beginQuiz())
                .size(PAGE_TEXT_WIDTH, 20)
                .pos(0, 0)
                .build());

        optionButtons.clear();
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button button = this.addRenderableWidget(Button.builder(Component.literal(""), b -> answer(idx))
                    .size(PAGE_TEXT_WIDTH, 20)
                    .pos(0, 0)
                    .build());
            button.visible = false;
            button.active = false;
            optionButtons.add(button);
        }

        rebuildPages();
        positionWidgets();
        updateWidgetStates();
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        positionWidgets();
    }

    private void positionWidgets() {
        int bookLeft = (this.width - BOOK_WIDTH) / 2;
        int bookTop = (this.height - BOOK_HEIGHT) / 2;

        if (previousPageButton != null) {
            previousPageButton.setPosition(bookLeft + 20, bookTop + 154);
        }
        if (nextPageButton != null) {
            nextPageButton.setPosition(bookLeft + BOOK_WIDTH - 20 - PageButton.WIDTH, bookTop + 154);
        }

        if (beginButton != null) {
            beginButton.setPosition(bookLeft + RIGHT_PAGE_X,
                    bookTop + PAGE_TEXT_Y + PAGE_TEXT_HEIGHT - 120);
        }

        int answerX = bookLeft + RIGHT_PAGE_X;
        int quizPromptLines = this.font.split(Component.translatable("screen.cultivatereg.manual.quiz_prompt"), PAGE_TEXT_WIDTH).size();
        int answerY = bookTop + PAGE_TEXT_Y + quizPromptLines * this.font.lineHeight + this.font.lineHeight;
        for (int i = 0; i < optionButtons.size(); i++) {
            Button button = optionButtons.get(i);
            button.setPosition(answerX, answerY + i * (button.getHeight() + 4));
        }
    }

    private void rebuildPages() {
        pages.clear();
        // 1. Amalgamate all text components into a single list.
        List<Component> allParagraphs = new ArrayList<>();

        Component intro = alreadyMastered
                ? Component.translatable("screen.cultivatereg.manual.already_mastered").withStyle(s -> s.withColor(0x55FFFF))
                : Component.translatable("screen.cultivatereg.manual.instructions").withStyle(s -> s.withColor(INSTRUCTION_TEXT_COLOR));
        allParagraphs.add(intro);
        allParagraphs.add(Component.translatable("screen.cultivatereg.manual.page_hint").withStyle(s -> s.withColor(PAGE_HINT_COLOR)));
        allParagraphs.add(Component.empty());


        allParagraphs.add(Component.translatable("screen.cultivatereg.manual.section.description")
                .withStyle(style -> style.withColor(SECTION_HEADING_COLOR).withBold(true)));
        addMultilineParagraph(allParagraphs, manual.description(), BODY_TEXT_COLOR);
        allParagraphs.add(Component.empty());

        allParagraphs.add(PAGE_BREAK);

        allParagraphs.add(Component.translatable("screen.cultivatereg.manual.section.content")
                .withStyle(style -> style.withColor(SECTION_HEADING_COLOR).withBold(true)));
        addMultilineParagraph(allParagraphs, manual.content(), BODY_TEXT_COLOR);
        allParagraphs.add(Component.empty());

        allParagraphs.add(PAGE_BREAK);

        allParagraphs.add(Component.translatable("screen.cultivatereg.manual.section.requirement")
                .withStyle(style -> style.withColor(SECTION_HEADING_COLOR).withBold(true)));
        addMultilineParagraph(allParagraphs, manual.breakthroughRequirement(), BODY_TEXT_COLOR);
        allParagraphs.add(Component.empty());

        allParagraphs.add(Component.translatable("screen.cultivatereg.manual.final_prompt")
                .withStyle(style -> style.withColor(FINAL_PROMPT_COLOR)));
        allParagraphs.add(Component.empty());


        // 2. Process the amalgamated list into pages with a single, consistent logic.
        List<PageLine> currentPage = new ArrayList<>();
        int baseLineCount = Math.max(1, (PAGE_TEXT_HEIGHT - PAGE_BOTTOM_MARGIN) / this.font.lineHeight);
        int maxLines = Math.max(1, baseLineCount - 1);

        for (Component component : allParagraphs) {
            if (component == PAGE_BREAK) {
                // If the current page isn't empty, add it to the book and start a fresh page.
                if (!currentPage.isEmpty()) {
                    pages.add(List.copyOf(currentPage));
                    currentPage.clear();
                }
                continue; // Skip processing this marker as text
            }
            // Handle empty components which represent blank lines.
            if (component.getString().isEmpty()) {
                if (currentPage.size() >= maxLines) {
                    pages.add(List.copyOf(currentPage));
                    currentPage.clear();
                }
                currentPage.add(new PageLine(FormattedCharSequence.forward("", Style.EMPTY), BODY_TEXT_COLOR));
                continue;
            }

            int color = getComponentColor(component, BODY_TEXT_COLOR);
            List<FormattedCharSequence> lines = this.font.split(component, PAGE_TEXT_WIDTH);

            // Add each line to the current page, creating a new page when full.
            for (FormattedCharSequence line : lines) {
                if (currentPage.size() >= maxLines) {
                    pages.add(List.copyOf(currentPage));
                    currentPage.clear();
                }
                currentPage.add(new PageLine(line, color));
            }
        }

        // 3. Add the final page if it has any content.
        if (!currentPage.isEmpty()) {
            pages.add(List.copyOf(currentPage));
        }

        // 4. Ensure there's at least one page, even if empty.
        if (pages.isEmpty()) {
            pages.add(Collections.emptyList());
        }

        spreadIndex = Mth.clamp(spreadIndex, 0, getMaxSpreadIndex());
    }

    private void addMultilineParagraph(List<Component> paragraphs, String rawText, int color) {
        if (rawText == null || rawText.isEmpty()) {
            return;
        }
        String[] lines = rawText.replace("\r", "").split("\n");
        for (String line : lines) {
            if (line.isBlank()) {
                paragraphs.add(Component.empty());
            } else {
                paragraphs.add(Component.literal(line.strip()).withStyle(style -> style.withColor(color)));
            }
        }
    }

    private void beginQuiz() {
        questionIndex = 0;
        feedback = Component.empty().copy();
        updateWidgetStates();
        showCurrentQuestion();
    }

    private void previousSpread() {
        if (isQuizActive()) {
            return;
        }
        if (spreadIndex > 0) {
            spreadIndex--;
        }
        updateWidgetStates();
    }

    private void nextSpread() {
        if (isQuizActive()) {
            return;
        }
        if (spreadIndex < getMaxSpreadIndex()) {
            spreadIndex++;
        }
        updateWidgetStates();
    }

    private void answer(int optionIndex) {
        if (questionIndex < 0 || questionIndex >= manual.quiz().size()) {
            return;
        }

        var question = manual.quiz().get(questionIndex);
        if (question.correctIndices().contains(optionIndex)) {
            questionIndex++;
            if (questionIndex >= manual.quiz().size()) {
                feedback = Component.translatable("screen.cultivatereg.manual.feedback.complete", manual.displayName())
                        .withStyle(ChatFormatting.GOLD);
                if (!completionSent) {
                    Net.CHANNEL.sendToServer(new ManualQuizCompletePacket(manual.id()));
                    completionSent = true;
                }
                questionIndex = -1;
                spreadIndex = getMaxSpreadIndex();
                updateWidgetStates();
            } else {
                feedback = Component.translatable("screen.cultivatereg.manual.feedback.correct")
                        .withStyle(ChatFormatting.GREEN);
                showCurrentQuestion();
            }
        } else {
            feedback = Component.translatable("screen.cultivatereg.manual.feedback.incorrect")
                    .withStyle(ChatFormatting.RED);
        }
    }

    private void showCurrentQuestion() {
        if (!isQuizActive()) {
            optionButtons.forEach(btn -> {
                btn.visible = false;
                btn.active = false;
            });
            return;
        }

        var question = manual.quiz().get(questionIndex);
        for (int i = 0; i < optionButtons.size(); i++) {
            Button button = optionButtons.get(i);
            if (i < question.options().size()) {
                button.setMessage(Component.literal(question.options().get(i)));
                button.visible = true;
                button.active = true;
            } else {
                button.visible = false;
                button.active = false;
            }
        }
    }

    private void updateWidgetStates() {
        boolean reading = !isQuizActive();
        int maxSpread = getMaxSpreadIndex();

        if (previousPageButton != null) {
            previousPageButton.visible = reading;
            previousPageButton.active = reading && spreadIndex > 0;
        }
        if (nextPageButton != null) {
            nextPageButton.visible = reading;
            nextPageButton.active = reading && spreadIndex < maxSpread;
        }

        if (beginButton != null) {
            if (completionSent) {
                beginButton.setMessage(Component.translatable("screen.cultivatereg.manual.retake"));
            } else {
                beginButton.setMessage(Component.translatable("screen.cultivatereg.manual.begin"));
            }
            beginButton.visible = reading && spreadIndex >= maxSpread;
            beginButton.active = beginButton.visible;
        }

        if (!reading) {
            showCurrentQuestion();
        } else {
            optionButtons.forEach(btn -> {
                btn.visible = false;
                btn.active = false;
            });
        }
    }

    private int getMaxSpreadIndex() {
        int spreadCount = (pages.size() + 1) / 2;
        return Math.max(0, spreadCount - 1);
    }

    private boolean isQuizActive() {
        return questionIndex >= 0 && questionIndex < manual.quiz().size();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int bookLeft = (this.width - BOOK_WIDTH) / 2;
        int bookTop = (this.height - BOOK_HEIGHT) / 2;

        graphics.blit(BOOK_TEXTURE, bookLeft, bookTop, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);

        // --- RENDER MULTI-LINE TITLE ---
       // Component title = Component.translatable("screen.cultivatereg.manual.heading", manual.displayName());
       // int titleMaxWidth = BOOK_WIDTH - 44; // Give 22px margin on each side to match page text
       // int bookCenterX = bookLeft + BOOK_WIDTH / 2;
       // int currentY = bookTop + 12;

       // for (FormattedCharSequence line : this.font.split(title, titleMaxWidth)) {
        //    int lineWidth = this.font.width(line);
         //   graphics.drawString(this.font, line, bookCenterX - lineWidth / 2, currentY, 0x3F3F3F, false);
         //   currentY += this.font.lineHeight;
        // }
        // --- END OF TITLE RENDER ---

        if (!manualStack.isEmpty()) {
            graphics.renderItem(manualStack, bookLeft + 8, bookTop + 8);
        }

        if (isQuizActive()) {
            renderQuiz(graphics, bookLeft, bookTop);
        } else {
            renderPages(graphics, bookLeft, bookTop);
        }

        if (!feedback.getString().isEmpty()) {
            drawCenteredComponent(graphics, feedback, this.width / 2, bookTop + BOOK_HEIGHT + 6, 0xFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPages(GuiGraphics graphics, int bookLeft, int bookTop) {
        int leftIndex = spreadIndex * 2;
        int rightIndex = leftIndex + 1;

        drawPage(graphics, bookLeft + LEFT_PAGE_X, bookTop + PAGE_TEXT_Y, leftIndex);
        drawPage(graphics, bookLeft + RIGHT_PAGE_X, bookTop + PAGE_TEXT_Y, rightIndex);

        Component pageIndicator = Component.translatable("screen.cultivatereg.manual.page_indicator",
                Math.min(leftIndex + 1, pages.size()), pages.size());
        drawCenteredComponent(graphics, pageIndicator, bookLeft + BOOK_WIDTH / 2, bookTop + BOOK_HEIGHT - 18, PAGE_INDICATOR_COLOR);
    }

    private void drawPage(GuiGraphics graphics, int x, int y, int pageIndex) {
        if (pageIndex < 0 || pageIndex >= pages.size()) {
            return;
        }

        int lineY = y;
        for (PageLine line : pages.get(pageIndex)) {
            graphics.drawString(this.font, line.text(), x, lineY, line.color(), false);
            lineY += this.font.lineHeight;
        }
    }

    private void renderQuiz(GuiGraphics graphics, int bookLeft, int bookTop) {
        if (questionIndex < 0 || questionIndex >= manual.quiz().size()) {
            return;
        }

        var question = manual.quiz().get(questionIndex);
        int promptX = bookLeft + LEFT_PAGE_X;
        int promptY = bookTop + PAGE_TEXT_Y;

        graphics.drawString(this.font,
                Component.translatable("screen.cultivatereg.manual.quiz_heading"),
                promptX, promptY, QUIZ_HEADING_COLOR, false);

        int y = promptY + this.font.lineHeight + 2;
        for (FormattedCharSequence line : this.font.split(Component.literal(question.prompt()), PAGE_TEXT_WIDTH)) {
            graphics.drawString(this.font, line, promptX, y, QUIZ_TEXT_COLOR, false);
            y += this.font.lineHeight;
        }

        int instructionsX = bookLeft + RIGHT_PAGE_X;
        Component prompt = Component.translatable("screen.cultivatereg.manual.quiz_prompt");
        int instructionY = promptY;
        for (FormattedCharSequence line : this.font.split(prompt, PAGE_TEXT_WIDTH)) {
            graphics.drawString(this.font, line, instructionsX, instructionY, QUIZ_TEXT_COLOR, false);
            instructionY += this.font.lineHeight;
        }
    }

    private void drawCenteredComponent(GuiGraphics graphics, Component component, int centerX, int y, int fallbackColor) {
        int color = getComponentColor(component, fallbackColor);
        int x = centerX - this.font.width(component) / 2;
        graphics.drawString(this.font, component, x, y, color, false);
    }

    private static int getComponentColor(Component component, int fallbackColor) {
        var color = component.getStyle().getColor();
        return color != null ? color.getValue() : fallbackColor;
    }

    private record PageLine(FormattedCharSequence text, int color) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isQuizActive()) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        if (delta > 0) {
            previousSpread();
        } else if (delta < 0) {
            nextSpread();
        } else {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isQuizActive()) {
            if (keyCode == InputConstants.KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
                previousSpread();
                return true;
            }
            if (keyCode == InputConstants.KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_SPACE) {
                nextSpread();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class PageButton extends AbstractButton {
        private static final int WIDTH = 23;
        private static final int HEIGHT = 13;

        private final boolean forward;
        private final Runnable onPress;

        PageButton(int x, int y, boolean forward, Runnable onPress) {
            super(x, y, WIDTH, HEIGHT, Component.empty());
            this.forward = forward;
            this.onPress = onPress;
        }

        @Override
        public void onPress() {
            onPress.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int u = this.isHoveredOrFocused() ? WIDTH : 0;
            int v = forward ? 192 : 192 + HEIGHT;
            graphics.blit(BOOK_TEXTURE, this.getX(), this.getY(), u, v, WIDTH, HEIGHT);
        }
    }
}

