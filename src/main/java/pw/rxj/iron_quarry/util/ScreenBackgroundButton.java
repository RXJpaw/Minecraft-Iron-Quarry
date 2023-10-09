package pw.rxj.iron_quarry.util;

public class ScreenBackgroundButton {
    private int buttonX;
    private int buttonY;
    private int mouseX;
    private int mouseY;
    private int buttonHeight;
    private int buttonWidth;

    private float wasMouseOverMillis = 0;

    public ScreenBackgroundButton(int buttonX, int buttonY, int buttonWidth, int buttonHeight, int mouseX, int mouseY){
        this.setAll(buttonX, buttonY, buttonWidth, buttonHeight, mouseX, mouseY);
    }
    public ScreenBackgroundButton(int buttonX, int buttonY, int buttonWidth, int buttonHeight){
        this.setAll(buttonX, buttonY, buttonWidth, buttonHeight, -1, -1);
    }
    public ScreenBackgroundButton(){ }

    public boolean isMouseOver(float delta, float maxDelta){
        boolean wasMouseOver = mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;

        if(wasMouseOver) {
            wasMouseOverMillis = Math.min(wasMouseOverMillis+delta, maxDelta);
        } else {
            wasMouseOverMillis = Math.max(wasMouseOverMillis-delta, 0);
        }

        return wasMouseOver;
    }


    public boolean isMouseOver(){
        return mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;
    }


    public float wasMouseOverMillis(){
        return wasMouseOverMillis;
    }

    public int getButtonX() {
        return buttonX;
    }
    public int getButtonY() {
        return buttonY;
    }
    public int getMouseX() {
        return mouseX;
    }
    public int getMouseY() {
        return mouseY;
    }
    public int getButtonHeight() {
        return buttonHeight;
    }
    public int getButtonWidth() {
        return buttonWidth;
    }

    public ScreenBackgroundButton setAll(int buttonX, int buttonY, int buttonWidth, int buttonHeight, int mouseX, int mouseY){
        if(this.buttonX != -1) this.buttonX = buttonX;
        if(this.buttonY != -1) this.buttonY = buttonY;
        if(this.buttonWidth != -1) this.buttonWidth = buttonWidth;
        if(this.buttonHeight != -1) this.buttonHeight = buttonHeight;
        if(this.mouseX != -1) this.mouseX = mouseX;
        if(this.mouseY != -1) this.mouseY = mouseY;

        return this;
    }

    public ScreenBackgroundButton setMousePos(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
        return this;
    }
}

