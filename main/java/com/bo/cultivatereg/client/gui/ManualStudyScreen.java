package com.bo.cultivatereg.client.gui;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.manual.CultivationManual;
import com.bo.cultivatereg.network.ManualQuizCompletePacket;
import com.bo.cultivatereg.network.Net;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the text of a cultivation manual and runs the associated comprehension quiz.
 */
@OnlyIn(Dist.CLIENT)
public class ManualStudyScreen extends Screen {
    private static final int CONTENT_WIDTH = 240;

    private final CultivationManual manual;
    private final ItemStack manualStack;

    private final List<Button> optionButtons = new ArrayList<>();
    private Button beginButton;

    private int questionIndex = -1;
    private boolean completionSent = false;
    private MutableComponent feedback = Component.empty().copy();
    private boolean alreadyMastered = false;

    private double textScrollOffset = 0;
    private int textAreaTop = 0;
    private int textAreaHeight = 0;
    private int textAreaLeft = 0;
    private int manualContentHeight = 0;
    private int maxScroll = 0;

    public ManualStudyScreen(CultivationManual manual, ItemStack stack) {
        super(Component.literal(manual.displayName()));
        this.manual = manual;
        this.manualStack = stack.copy();
    }

    public static void open(CultivationManual manual, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.setScreen(new ManualStudyScreen(manual, stack));
    }

    @Override
    protected void init() {
        super.init();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data ->
                    alreadyMastered = data.isManualQuizPassed() && manual.id().equals(data.getManualId()));
        }
        if (alreadyMastered) {
            completionSent = true;
        }

        this.beginButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.cultivatereg.manual.begin"),
                        b -> beginQuiz())
                .pos(this.width / 2 - 90, this.height - 34)
                .size(180, 20)
                .build());

        int baseY = this.height - 134;
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button button = this.addRenderableWidget(Button.builder(Component.literal(""),
                            b -> answer(idx))
                    .pos(this.width / 2 - 120, baseY + i * 28)
                    .size(240, 24)
                    .build());
            button.visible = false;
            button.active = false;
            optionButtons.add(button);
        }

        updateButtonVisibility();
    }

    private void beginQuiz() {
        questionIndex = 0;
        feedback = Component.empty().copy();
        textScrollOffset = 0;
        updateButtonVisibility();
    }

    private void answer(int optionIndex) {
        if (questionIndex < 0 || questionIndex >= manual.quiz().size()) {
            return;
        }

        var question = manual.quiz().get(questionIndex);
        if (optionIndex == question.correctIndex()) {
            questionIndex++;
            if (questionIndex >= manual.quiz().size()) {
                feedback = Component.translatable("screen.cultivatereg.manual.feedback.complete", manual.displayName())
                        .withStyle(ChatFormatting.GOLD);
                optionButtons.forEach(btn -> {
                    btn.visible = false;
                    btn.active = false;
                });
                if (!completionSent) {
                    Net.CHANNEL.sendToServer(new ManualQuizCompletePacket(manual.id()));
                    completionSent = true;
                }
                updateButtonVisibility();
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
        if (questionIndex < 0 || questionIndex >= manual.quiz().size()) {
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

    private void updateButtonVisibility() {
        boolean askingQuestion = questionIndex >= 0 && questionIndex < manual.quiz().size();
        boolean canStart = questionIndex < 0 || questionIndex >= manual.quiz().size();
        beginButton.visible = canStart;
        beginButton.active = canStart;
        if (completionSent) {
            beginButton.setMessage(Component.translatable("screen.cultivatereg.manual.retake"));
        } else {
            beginButton.setMessage(Component.translatable("screen.cultivatereg.manual.begin"));
        }

        if (askingQuestion) {
            showCurrentQuestion();
        } else {
            optionButtons.forEach(btn -> {
                btn.visible = false;
                btn.active = false;
            });
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int left = (this.width - CONTENT_WIDTH) / 2;
        this.textAreaLeft = left;

        MutableComponent instruction = alreadyMastered
                ? Component.translatable("screen.cultivatereg.manual.already_mastered").withStyle(ChatFormatting.AQUA)
                : Component.translatable("screen.cultivatereg.manual.instructions").withStyle(ChatFormatting.GRAY);
        graphics.drawCenteredString(this.font, instruction, this.width / 2, 14, 0xFFFFFF);

        int headingY = 32;
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.cultivatereg.manual.heading", manual.displayName()),
                this.width / 2, headingY, 0xF7E8A0);

        boolean askingQuestion = questionIndex >= 0 && questionIndex < manual.quiz().size();

        int manualTop = headingY + 24;
        int quizHeadingTop = this.height - 160;
        this.textAreaTop = askingQuestion ? 0 : manualTop;
        int calculatedHeight = quizHeadingTop - 12 - manualTop;
        this.textAreaHeight = askingQuestion ? 0 : Math.max(0, calculatedHeight);

        if (!askingQuestion && !manualStack.isEmpty()) {
            graphics.renderItem(manualStack, left - 28, manualTop);
        }

        if (!askingQuestion && this.textAreaHeight > 0) {
            manualContentHeight = computeManualContentHeight();
            maxScroll = Math.max(0, manualContentHeight - textAreaHeight);
            textScrollOffset = Mth.clamp(textScrollOffset, 0, maxScroll);

            graphics.enableScissor(left, textAreaTop, left + CONTENT_WIDTH, textAreaTop + textAreaHeight);

            int scrollY = textAreaTop - (int) Math.round(textScrollOffset);
            scrollY = drawScrollableSection(graphics, Component.translatable("screen.cultivatereg.manual.section.description"),
                    Component.literal(manual.description()), left, scrollY, 0xFFFFFF, textAreaTop, textAreaTop + textAreaHeight);
            scrollY = drawScrollableSection(graphics, Component.translatable("screen.cultivatereg.manual.section.content"),
                    Component.literal(manual.content()), left, scrollY + 6, 0xFFEAB5, textAreaTop, textAreaTop + textAreaHeight);
            drawScrollableSection(graphics, Component.translatable("screen.cultivatereg.manual.section.requirement"),
                    Component.literal(manual.breakthroughRequirement()), left, scrollY + 6, 0xCFE9FF, textAreaTop, textAreaTop + textAreaHeight);

            graphics.disableScissor();

            if (maxScroll > 0) {
                drawScrollbar(graphics);
            }
        } else {
            manualContentHeight = 0;
            maxScroll = 0;
        }

        if (askingQuestion) {
            var question = manual.quiz().get(questionIndex);
            int questionTop = this.height - 160;
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.cultivatereg.manual.quiz_heading"),
                    this.width / 2, questionTop, 0xFFD780);
            int qY = questionTop + 12;
            for (FormattedCharSequence line : this.font.split(Component.literal(question.prompt()), CONTENT_WIDTH)) {
                graphics.drawString(this.font, line, left, qY, 0xFFFFFF, false);
                qY += 9;
            }
        } else {
            graphics.drawCenteredString(this.font,
                    Component.translatable("screen.cultivatereg.manual.quiz_heading"),
                    this.width / 2, this.height - 160, 0xFFD780);
        }

        if (!feedback.getString().isEmpty()) {
            graphics.drawCenteredString(this.font, feedback, this.width / 2, this.height - 18, 0xFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int drawScrollableSection(GuiGraphics graphics, Component header, Component body, int left, int startY, int headerColor,
                                      int clipTop, int clipBottom) {
        int lineHeight = this.font.lineHeight;
        if (isLineVisible(startY, clipTop, clipBottom, lineHeight)) {
            graphics.drawString(this.font, header, left, startY, headerColor, false);
        }
        int y = startY + 10;
        for (FormattedCharSequence line : this.font.split(body, CONTENT_WIDTH)) {
            if (isLineVisible(y, clipTop, clipBottom, lineHeight)) {
                graphics.drawString(this.font, line, left, y, 0xFFFFFF, false);
            }
            y += lineHeight;
        }
        return y;
    }

    private boolean isLineVisible(int lineY, int clipTop, int clipBottom, int lineHeight) {
        int bottom = lineY + lineHeight;
        return bottom >= clipTop && lineY <= clipBottom;
    }

    private int computeManualContentHeight() {
        int lineHeight = this.font.lineHeight;
        int height = sectionHeight(Component.literal(manual.description()), lineHeight);
        height += 6;
        height += sectionHeight(Component.literal(manual.content()), lineHeight);
        height += 6;
        height += sectionHeight(Component.literal(manual.breakthroughRequirement()), lineHeight);
        return height;
    }

    private int sectionHeight(Component body, int lineHeight) {
        int lines = this.font.split(body, CONTENT_WIDTH).size();
        return 10 + lines * lineHeight;
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int barWidth = 3;
        int barX = this.textAreaLeft + CONTENT_WIDTH - barWidth;
        int barTop = this.textAreaTop;
        int barBottom = this.textAreaTop + this.textAreaHeight;
        graphics.fill(barX, barTop, barX + barWidth, barBottom, 0x66000000);

        int thumbHeight = Math.max(8, (int) ((float) this.textAreaHeight * this.textAreaHeight / this.manualContentHeight));
        int thumbRange = this.textAreaHeight - thumbHeight;
        int thumbOffset = this.maxScroll == 0 ? 0 : (int) (textScrollOffset / maxScroll * thumbRange);
        graphics.fill(barX, barTop + thumbOffset, barX + barWidth, barTop + thumbOffset + thumbHeight, 0xCCF7E8A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.textAreaHeight > 0 && this.manualContentHeight > this.textAreaHeight) {
            if (mouseX >= this.textAreaLeft && mouseX <= this.textAreaLeft + CONTENT_WIDTH
                    && mouseY >= this.textAreaTop && mouseY <= this.textAreaTop + this.textAreaHeight) {
                double amount = delta * 10;
                textScrollOffset = Mth.clamp(textScrollOffset - amount, 0, this.maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}