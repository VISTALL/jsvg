/*
 * MIT License
 *
 * Copyright (c) 2023 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.jsvg.parser.css.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import com.github.weisj.jsvg.parser.css.CssParser;
import com.github.weisj.jsvg.parser.css.StyleProperty;
import com.github.weisj.jsvg.parser.css.StyleSheet;

public class SimpleCssParser implements CssParser {

    private static final Logger LOGGER = Logger.getLogger(SimpleCssParser.class.getName());

    @Override
    public @NotNull StyleSheet parse(@NotNull List<char[]> input) {
        return new Parser(input).parse();
    }


    private static class ParserException extends RuntimeException {
    }

    private static final class Parser {

        private final @NotNull Lexer lexer;
        private final @NotNull SimpleStyleSheet sheet = new SimpleStyleSheet();
        private @NotNull Token current;


        private Parser(@NotNull List<char[]> input) {
            this.lexer = new Lexer(input);
            this.current = lexer.nextToken();
        }

        private void next() {
            current = lexer.nextToken();
        }

        private void expected(@NotNull String type) {
            LOGGER.warning("Expected '" + type + "' but got '" + current + "'");
        }

        private void consume(TokenType type) {
            if (current.type() != type) {
                expected(type.toString());
                throw new ParserException();
            }
            next();
        }

        private @NotNull String consumeValue(TokenType type) {
            if (current.type() != type) {
                expected(type.toString());
                throw new ParserException();
            }
            if (current.data() == null) {
                throw new ParserException();
            }
            String value = Objects.requireNonNull(current.data());
            next();
            return value;
        }

        private @NotNull List<Token> readIdentifierList() {
            List<Token> list = new ArrayList<>();

            while (current.type() != TokenType.CURLY_OPEN && current.type() != TokenType.EOF) {
                TokenType type = current.type();
                if (type != TokenType.IDENTIFIER
                        && type != TokenType.ID_NAME
                        && type != TokenType.CLASS_NAME) {
                    expected("identifier");
                    throw new ParserException();
                }
                list.add(current);
                next();

                if (current.type() == TokenType.COMMA) {
                    next();
                } else {
                    break;
                }
            }
            return list;
        }

        private @NotNull List<StyleProperty> readProperties() {
            List<StyleProperty> list = new ArrayList<>();

            consume(TokenType.CURLY_OPEN);

            while (current.type() != TokenType.CURLY_CLOSE && current.type() != TokenType.EOF) {
                String name = consumeValue(TokenType.IDENTIFIER);
                consume(TokenType.COLON);
                String value = consumeValue(TokenType.RAW_DATA);
                consume(TokenType.SEMICOLON);
                list.add(new StyleProperty(name, value));
            }

            consume(TokenType.CURLY_CLOSE);
            return list;
        }

        private void skipToNextDefinition() {
            while (current.type() != TokenType.CURLY_CLOSE) {
                next();
            }
            next();
        }

        @NotNull
        SimpleStyleSheet parse() {
            while (current.type() != TokenType.EOF) {
                try {
                    List<Token> identifierList = readIdentifierList();
                    List<StyleProperty> properties = readProperties();

                    for (Token token : identifierList) {
                        switch (token.type()) {
                            case CLASS_NAME:
                                sheet.addClassRules(Objects.requireNonNull(token.data()), properties);
                                break;
                            case ID_NAME:
                                sheet.addIdRules(Objects.requireNonNull(token.data()), properties);
                                break;
                            case IDENTIFIER:
                                sheet.addTagNameRules(Objects.requireNonNull(token.data()), properties);
                                break;
                            default:
                                throw new IllegalStateException("Toke = " + token);
                        }
                    }
                } catch (ParserException e) {
                    skipToNextDefinition();
                }
            }
            return sheet;
        }
    }
}
