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
        float scale = 2.0f;

        float bookWidth = page.getWidth() * scale;
        float bookHeight = page.getHeight() * scale;

        float x = (screenWidth - bookWidth) * 0.5f;
        float y = (screenHeight - bookHeight) * 0.5f;

        GUI.drawTexture(page, x, y, bookWidth, bookHeight, new Vector4f(1.0f));
        renderPage(book, x, y);
    }

    private static void renderPage(Book book, float x, float y) {
        if (book.getPages().isEmpty()) {
            return;
        }

        Page page = book.getPage(book.getCurrentPage());
        float paddingX = K.UI.UI_BOOK_PADDING_X;
        float paddingTop = K.UI.UI_BOOK_PADDING_TOP;
        float lineHeight = GUI.getNormalFont().getSize() * 2.0f;

        float textX = x + paddingX;
        float textY = y + paddingTop;

        for (String line : page.getLines()) {
            if (line.startsWith("**")) {
                line = line.replace("**", "");
                GUI.drawBoldString(line, textX, textY, K.UI.UI_BOOK_TEXT_COLOR);
            } else {
                GUI.drawNormalString(line, textX, textY, K.UI.UI_BOOK_TEXT_COLOR);
            }
            textY += lineHeight;
        }
    }
}