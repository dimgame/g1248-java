/* license: https://mit-license.org
 * ==============================================================================
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Albert Moky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ==============================================================================
 */
package chat.dim.g1248.cpu;

import java.util.List;

import chat.dim.Facebook;
import chat.dim.Messenger;
import chat.dim.cpu.CustomizedContentHandler;
import chat.dim.cpu.CustomizedContentProcessor;
import chat.dim.g1248.handler.HallContentHandler;
import chat.dim.g1248.handler.TableContentHandler;
import chat.dim.g1248.protocol.GameContent;
import chat.dim.g1248.protocol.HallContent;
import chat.dim.g1248.protocol.TableContent;
import chat.dim.protocol.Content;
import chat.dim.protocol.CustomizedContent;
import chat.dim.protocol.ReliableMessage;

/**
 *  Application Customized Content Processor
 *  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 *  Process customized contents for this application only
 */
public class AppContentProcessor extends CustomizedContentProcessor {

    // module(s) for customized contents
    private final CustomizedContentHandler hallHandler;
    private final CustomizedContentHandler tableHandler;

    public AppContentProcessor(Facebook facebook, Messenger messenger) {
        super(facebook, messenger);
        hallHandler = new HallContentHandler(facebook, messenger);
        tableHandler = new TableContentHandler(facebook, messenger);
    }

    @Override
    protected List<Content> filter(String app, CustomizedContent content, ReliableMessage rMsg) {
        if (app != null && app.equals(GameContent.APP_ID)) {
            // App ID match
            // return null to fetch module handler
            return null;
        }
        return super.filter(app, content, rMsg);
    }

    @Override
    protected CustomizedContentHandler fetch(String mod, CustomizedContent content, ReliableMessage rMsg) {
        if (mod == null) {
            throw new IllegalArgumentException("module name empty: " + content);
        } else if (mod.equals(HallContent.MOD_NAME)) {
            // customized module: "hall"
            return hallHandler;
        } else if (mod.equals(TableContent.MOD_NAME)) {
            // customized module: "table"
            return tableHandler;
        }
        // TODO: define your modules here
        // ...

        return super.fetch(mod, content, rMsg);
    }
}
