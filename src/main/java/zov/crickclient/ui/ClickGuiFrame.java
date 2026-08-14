package zov.crickclient.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import zov.crickclient.module.list.render.ClickGui;
import zov.crickclient.module.settings.ItemModelSetting;
import zov.crickclient.ui.component.ItemModelGalleryPopup;
import zov.crickclient.ui.component.SearchField;
import zov.crickclient.util.IMinecraft;
import zov.crickclient.util.base.Instance;
import zov.crickclient.util.cursor.CursorManager;
import zov.crickclient.util.render.math.Scissor;

@Getter
public class ClickGuiFrame extends Screen implements IMinecraft {

    private final ClickGuiShell shell;
    private final SearchField searchField;
    private final ThemeEditor themeEditor = new ThemeEditor();
    private ItemModelGalleryPopup itemModelGallery;

    private boolean closing;

    private long handCursor, iBeamCursor, pointingCursor, arrowCursor;
    private boolean cursorsCreated;
    private long currentCursor;

    private String cachedRawQuery = "";
    private String cachedNormalizedQuery = "";

    public ClickGuiFrame() {
        super(Text.of("CrickClient"));
        searchField = new SearchField("Search modules...");
        shell = new ClickGuiShell(this, searchField);
    }

    public void playOpenAnimation() {
        closing = false;
        itemModelGallery = null;
        shell.resetOpenAnimation();
        themeEditor.resetAppear();
        searchField.resetAppear();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        CursorManager.reset();
        CursorManager.resetIBeam();
        CursorManager.resetClick();

        int windowWidth = mc.getWindow().getScaledWidth();
        int windowHeight = mc.getWindow().getScaledHeight();

        float guiScale = guiScale();
        float centerX = windowWidth / 2f;
        float centerY = windowHeight / 2f;
        mouseX = (int) ((mouseX - centerX) / guiScale + centerX);
        mouseY = (int) ((mouseY - centerY) / guiScale + centerY);

        Scissor.setGuiTransform(guiScale, centerX, centerY);

        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.translate(centerX, centerY, 0f);
        modelView.scale(guiScale, guiScale, 1f);
        modelView.translate(-centerX, -centerY, 0f);

        float shiftX = themeEditor.getShellShiftX();
        float shiftY = themeEditor.getShellShiftY();
        shell.layout(windowWidth, windowHeight, shiftX, shiftY);
        shell.getOpenAnim().run(!closing);
        float open = MathHelper.clamp((float) shell.getOpenAnim().getValue(), 0f, 1f);
        ClickGuiStyles.drawBackdrop(windowWidth, windowHeight, Math.max(open, 0.35f));
        shell.render(context, mouseX, mouseY, delta, open);

        themeEditor.setAnchor(shell.getAnchorRight(), shell.getAnchorY(), shell.getAnchorHeight());
        themeEditor.render(context, mouseX, mouseY, delta, open);

        if (closing && open <= 0.02f) {
            closing = false;
            modelView.popMatrix();
            Scissor.resetGuiTransform();
            close();
            return;
        }

        if (itemModelGallery != null) {
            itemModelGallery.render(context, mouseX, mouseY, delta);
        }

        modelView.popMatrix();
        Scissor.resetGuiTransform();

        ensureCursors();
        long desiredCursor;
        if (CursorManager.shouldBeHand()) desiredCursor = handCursor;
        else if (CursorManager.shouldIBeam()) desiredCursor = iBeamCursor;
        else if (CursorManager.shouldClick()) desiredCursor = pointingCursor;
        else desiredCursor = arrowCursor;
        applyCursor(desiredCursor);
    }

    public boolean searchCheck(String text) {
        if (searchField.isEmpty()) return false;
        String raw = searchField.text;
        if (!raw.equals(cachedRawQuery)) {
            cachedRawQuery = raw;
            cachedNormalizedQuery = raw.replaceAll(" ", "").toLowerCase();
        }
        return !text.replaceAll(" ", "").toLowerCase().contains(cachedNormalizedQuery);
    }

    public void openItemModelGallery(ItemModelSetting setting) {
        itemModelGallery = new ItemModelGalleryPopup(setting);
    }

    private float guiScale() {
        ClickGui module = (ClickGui) Instance.get(ClickGui.class);
        float userScale = module != null ? module.size.getFloatValue() : 0.78f;

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        float totalW = ClickGuiStyles.SHELL_WIDTH + ThemeEditor.COLLAPSED_W + 20f;
        if (themeEditor.isExpanded()) {
            totalW += ThemeEditor.POPUP_W + 36f;
        }
        float totalH = ClickGuiStyles.SHELL_HEIGHT + 16f;

        float widthLimit = (screenW - 32f) / totalW;
        float heightLimit = (screenH - 32f) / totalH;
        float autoLimit = Math.min(widthLimit, heightLimit);

        return Math.min(Math.min(userScale, autoLimit), 1.1f);
    }

    private double scaleMouseX(double mouseX) {
        float s = guiScale();
        double cx = mc.getWindow().getScaledWidth() / 2.0;
        return (mouseX - cx) / s + cx;
    }

    private double scaleMouseY(double mouseY) {
        float s = guiScale();
        double cy = mc.getWindow().getScaledHeight() / 2.0;
        return (mouseY - cy) / s + cy;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX = scaleMouseX(mouseX);
        mouseY = scaleMouseY(mouseY);

        if (itemModelGallery != null) {
            if (!itemModelGallery.contains(mouseX, mouseY)) {
                itemModelGallery = null;
            } else {
                itemModelGallery.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        if (themeEditor.mouseClicked(mouseX, mouseY, button)) return true;
        if (shell.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouseX = scaleMouseX(mouseX);
        mouseY = scaleMouseY(mouseY);
        if (itemModelGallery != null) {
            itemModelGallery.mouseReleased(mouseX, mouseY, button);
            return true;
        }
        themeEditor.mouseReleased(mouseX, mouseY, button);
        shell.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        mouseX = scaleMouseX(mouseX);
        mouseY = scaleMouseY(mouseY);
        if (itemModelGallery != null) {
            itemModelGallery.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return true;
        }
        shell.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (itemModelGallery != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) itemModelGallery = null;
            else itemModelGallery.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && shell.isBindingAnyModule()) {
            shell.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (shell.isConfigFieldFocused()) {
            shell.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (searchField.isFocused()) {
            searchField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            ensureCursors();
            applyCursor(arrowCursor);
            closing = true;
            return true;
        }

        shell.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (itemModelGallery != null) {
            itemModelGallery.charTyped(chr, modifiers);
            return true;
        }
        if (shell.isConfigFieldFocused()) {
            shell.charTyped(chr, modifiers);
            return true;
        }
        if (searchField.isFocused()) {
            searchField.charTyped(chr, modifiers);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        if (cursorsCreated) {
            GLFW.glfwSetCursor(mc.getWindow().getHandle(), 0L);
            GLFW.glfwDestroyCursor(handCursor);
            GLFW.glfwDestroyCursor(iBeamCursor);
            GLFW.glfwDestroyCursor(pointingCursor);
            GLFW.glfwDestroyCursor(arrowCursor);
            cursorsCreated = false;
            currentCursor = 0L;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void ensureCursors() {
        if (cursorsCreated) return;
        handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        iBeamCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
        pointingCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_POINTING_HAND_CURSOR);
        arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
        cursorsCreated = true;
    }

    private void applyCursor(long cursor) {
        if (cursor == currentCursor) return;
        GLFW.glfwSetCursor(mc.getWindow().getHandle(), cursor);
        currentCursor = cursor;
    }
}
