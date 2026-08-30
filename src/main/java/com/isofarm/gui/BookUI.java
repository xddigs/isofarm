package com.isofarm.gui;

import com.isofarm.graphics.Texture;
import com.isofarm.item.Book;
import com.isofarm.item.Page;
import com.isofarm.utils.K;
import org.joml.Vector4f;

public class BookUI {
    private static final Texture page = new Texture(K.Paths.DEFAULT_BOOK_UI);

    public static void render(Book book) {
        if (book == null) {
            return;
        }

        float screenWidth = GUI.getScreenWidth();
        float screenHeight = GUI.getScreenHeight();

        float bookWidth = 500.0f;
        float bookHeight = 650.0f;

        float width = page.getWidth();
        float height = page.getHeight();
        float x = (screenWidth - width) * 0.5f;
        float y = (screenHeight - height) * 0.5f;

        GUI.drawTexture(page, x, y, width, height, new Vector4f(1.0f));
        renderPage(book, x, y, bookWidth, bookHeight);
    }

    private static void renderPage(Book book, float x, float y, float width, float height) {
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