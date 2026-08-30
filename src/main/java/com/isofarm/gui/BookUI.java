package com.isofarm.gui;

import com.isofarm.item.Book;
import com.isofarm.item.Page;
import com.isofarm.utils.K;

public class BookUI {

    public static void render(Book book) {
        if (book == null) {
            return;
        }

        float screenWidth = GUI.getScreenWidth();
        float screenHeight = GUI.getScreenHeight();

        float bookWidth = 500.0f;
        float bookHeight = 650.0f;

        float x = (screenWidth - bookWidth) * 0.5f;
        float y = (screenHeight - bookHeight) * 0.5f;

        GUI.drawRect(x, y, bookWidth, bookHeight, K.UI.UI_BACKGROUND_COLOR_SLOT);
        GUI.drawBorder(x, y, bookWidth, bookHeight, K.UI.UI_TEXT_COLOR, 3.0f);

        renderPage(book, x, y, bookWidth, bookHeight);
    }

    private static void renderPage(Book book, float x, float y,
                                   float width, float height) {
        if (book.getPages().isEmpty()) {
            return;
        }

        Page page = book.getPage(0);

        float padding = 40.0f;
        float lineHeight = 28.0f;

        float textX = x + padding;
        float textY = y + padding;

        for (String line : page.getLines()) {
            GUI.drawNormalString(line, textX, textY, K.UI.UI_TEXT_COLOR);
            textY += lineHeight;
        }
    }
}