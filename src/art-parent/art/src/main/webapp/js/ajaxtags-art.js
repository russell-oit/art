/**
 * Copyright 2007-2012 AjaxTags-Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*jslint bitwise: true, browser: true, eqeqeq: true, immed: true, newcap: true, nomen: true, regexp: true, undef: true, white: true, maxerr: 50, indent: 4 */
/*global $, $$, $F, Ajax, Autocompleter, Class, Element, Event, Field, Form, Option, Prototype, overlib */

//modifications to use jquery instead of prototype + scriptaculous
//// prototype and scriptaculous must be loaded before this script
//if ((typeof Prototype === 'undefined') || (typeof Element === 'undefined') || (typeof Element.Methods === 'undefined')) {
//    throw new Error("ajaxtags.js requires the Prototype JavaScript framework >= 1.6");
//}

var AjaxJspTag = {
    Version: '1.5.7',

    DEFAULT_PARAMETER: "ajaxParameter",
    VOID_URL: "javascript://nop",
    // unused? PORTLET_MAX: 1, PORTLET_MIN: 2, PORTLET_CLOSE: 3,
    CALLOUT_OVERLIB_DEFAULT: "STICKY,CLOSECLICK,DELAY,250,TIMEOUT,5000,VAUTO,WRAPMAX,240,CSSCLASS,FGCLASS,'olfg',BGCLASS,'olbg',CGCLASS,'olcg',CAPTIONFONTCLASS,'olcap',CLOSEFONTCLASS,'olclo',TEXTFONTCLASS,'oltxt'",

    /**
     * Store all tags which have listeners.
     */
    tags: [],
    /**
     * Push listener for later reload.
     */
    add: function (tag) {
        AjaxJspTag.tags.push(tag);
    },
    /**
     * Remove listener. This is not used. All listeners are reloaded after change
     * of content.
     */
    remove: function (tag) {
        AjaxJspTag.tags = AjaxJspTag.tags.without(tag);
    },
    /**
     * Reload the listeners. Any listener has a function called setListeners with
     * no arguments.
     * TODO reload after each request
     */
    reload: function () {
        AjaxJspTag.tags.each(function (tag) {
            if (Object.isFunction(tag.setListeners)) {
                tag.setListeners();
            }
        });
    }
};

AjaxJspTag.DEFAULT_PARAMETER_REGEXP = new RegExp("(\\{" + AjaxJspTag.DEFAULT_PARAMETER + "\\})", 'g');


/**
 * Response Parsers defaults and functions which can be used for HTML, TEXT and
 * XML. Call new DefaultResponseParser(TYPE) with the needed type ("html", "text", "xml").
 */
var DefaultResponseParser = Class.create({
    initialize: function (defaultType, isPlainText) {
        this.type = defaultType || "xml";
        this.plaintext = !!isPlainText;
        this.content = null; // reset in load
        this.contentText = null;
        this.contentXML = null;
    },
    load: function (response) {
        this.contentText = response.responseText;
        this.contentXML = response.responseXML;
        this.content = null; // init
        this.parse();
    },
    parse: function (forceType) {
        var type = forceType || this.type, content = null;
        if (type === "html") { // response is HTML
            content = this.contentText;
        } else if (type === "text") { // response is in CSV format
            content = [];
            this.contentText.split('\n').each(function (line) {
                content.push(line.split(','));
            });
        } else if (type === "xml") {
            content = this.parsexml();
        } else if (type === "xmltohtml") {
            content = this.parsexmltohtml();
        } else if (type === "xmltohtmllist") {
            content = this.parsexmltohtmllist();
        } else if (type === "plain") {
            content = this.contentText;
        }
        this.content = content;
    },
    parsexml: function () {
        var responseNodes = this.contentXML.documentElement.getElementsByTagName("response");
        var content = [];
        var i, j, k, len1 = responseNodes.length, len2, len3;
        var items, itemNode, nameNodes, valueNodes, nameNode, valueNode, row;
        for (i = 0; i < len1; i++) {
            items = responseNodes[i].getElementsByTagName("item");
            for (j = 0, len2 = items.length; j < len2; j++) {
                itemNode = items[j];
                nameNodes = itemNode.getElementsByTagName("name");
                valueNodes = itemNode.getElementsByTagName("value");
                for (k = 0, len3 = nameNodes.length, row = []; k < len3; k++) {
                    nameNode = nameNodes[k].firstChild;
                    row.push(nameNode ? nameNode.nodeValue : "");
                }
                if (row.length !== 1) {
                    throw new Error("Invalid XML format (item should have exactly one name)");
                }
                for (k = 0, len3 = valueNodes.length; k < len3; k++) {
                    valueNode = valueNodes[k].firstChild;
                    row.push(valueNode ? valueNode.nodeValue : "");
                }
                content.push(row);
            }
        }
        return content;
    },
    parsexmltohtml: function () {
        var content = new Element("div"), h1 = null, div = null;
        this.parsexml().each(function (row) {
            h1 = new Element("h1");
            if (this.plaintext) {
                h1.update(row[0]);
            } else {
                h1.innerHTML = row[0];
            }
            content.appendChild(h1);
            row.without(row[0]).each(function (line) {
                div = new Element("div");
                if (this.plaintext) {
                    div.update(line);
                } else {
                    div.innerHTML = line;
                }
                content.appendChild(div);
            }, this);
        }, this);
        return (h1 !== null) ? content.innerHTML : "";
    },
    parsexmltohtmllist: function () {
        var content = new Element("div"), ul = new Element("ul"), liElement = null;
        this.parsexml().each(function (row) {
            // FIXME prepend id with some text to avoid numeric ids
            liElement = new Element("li", {id: row[1]});
            if (this.plaintext) {
                liElement.update(row[0]);
            } else {
                liElement.innerHTML = row[0];
            }
            ul.appendChild(liElement);
        }, this);
        content.appendChild(ul);
        return (liElement !== null) ? content.innerHTML : "";
    }
});


var ResponseXmlToHtmlLinkListParser = Class.create(DefaultResponseParser, {
    initialize: function ($super) {
        $super("xmltohtmllinklist");
    },
    load: function ($super, response) {
        this.collapsedClass = response.collapsedClass;
        this.treeClass = response.treeClass;
        this.nodeClass = response.nodeClass;
        this.expandedNodes = [];
        $super(response);
    },
    parse: function () {
        var responseNodes = this.contentXML.documentElement.getElementsByTagName("response");
        if (responseNodes.length > 0) {
            var itemNodes = responseNodes[0].getElementsByTagName("item"), len = itemNodes.length;
            var ul = (len === 0) ? null : new Element("ul", {className: this.treeClass});
            var itemNode, nameNodes, valueNodes, urlNodes, collapsedNodes, leafNodes;
            var name, value, url, leaf, collapsed, li, link, div, i;
            for (i = 0; i < len; i++) {
                itemNode = itemNodes[i];
                nameNodes = itemNode.getElementsByTagName("name");
                valueNodes = itemNode.getElementsByTagName("value");
                urlNodes = itemNode.getElementsByTagName("url");
                collapsedNodes = itemNode.getElementsByTagName("collapsed");
                leafNodes = itemNode.getElementsByTagName("leaf");

                if (nameNodes.length > 0 && valueNodes.length > 0) {
                    name = nameNodes[0].firstChild.nodeValue;
                    value = valueNodes[0].firstChild.nodeValue;
                    url = (urlNodes.length > 0) ? urlNodes[0].firstChild.nodeValue : AjaxJspTag.VOID_URL;
                    leaf = (leafNodes.length > 0) && (leafNodes[0].firstChild.nodeValue).toLowerCase() === "true";
                    collapsed = (collapsedNodes.length > 0) && (collapsedNodes[0].firstChild.nodeValue).toLowerCase() === "true";

                    li = new Element("li", {id: "li_" + value});
                    if (!leaf) {
                        li.appendChild(new Element("span", {id: "span_" + value, className: this.collapsedClass}));
                    }
                    // TODO CSS classes for leaf and branch nodes
                    link = new Element("a", {href: url, className: this.nodeClass}).update(name);
                    div = new Element("div", {id: "div_" + value}).hide();
                    if (!collapsed) {
                        this.expandedNodes.push(value);
                    }
                    li.appendChild(link);
                    li.appendChild(div);
                    ul.appendChild(li);
                }
            }
            this.content = ul;
        }
    }
}); // end of parser


/****************************************************************
 * http://www.prototypejs.org/learn/class-inheritance
 *  -----
 *  AjaxJspTag.Base übernimmt die logik des daten holen.
 *  d.h. hier wird die preFunction ausgewertet und postFunction!
 *  in der theorie kann ich alles an prototype übergeben, da einige
 *  functionen abgebildet sind!
 *
 *  ----
 *  momentane fehler, die buildfunctionen sind nicht mehr vorhanden
 *  alle listenerfunctionen müssen mit dem reload ferbunden werden
 *
 *  ---
 *  (check) prototype.js Leerer String an getElementById() übergeben.
 *
 ****************************************************************/
/**
 * AjaxTags.
 */
AjaxJspTag.Base = Class.create({
    initialize: function (options) {
        this.setOptions(options);
        this.createElements();
        this.createListeners();
        this.setListeners();
        AjaxJspTag.add(this);
    },
    getDefaultOptions: function () {
        // override in descendants
        return {};
    },
    setOptions: function (options) {
        // override in descendants
        this.options = Object.extend(this.getDefaultOptions(), options || {});
    },
    createElements: function () {
        // override in descendants
    },
    createListeners: function () {
        // override in descendants
    },
    setListeners: function () {
        // override in descendants
    },
    resolveParameters: function () {
        var o = this.options, url = o.baseUrl;
        if (!Object.isString(url) || url.strip().length === 0) {
            throw new Error("URL is empty or undefined");
        }

        var q = url.split('?'), p = [];
        o.baseUrl = q[0];
        if (o.parameters) {
            p.push(o.parameters);
        }
        if (q[1]) {
            p.push(q[1].replace(/&/g, ','));
        }
        o.parameters = p.join(',');
    },
    getMethod: function () {
        return "post";
    },
    initRequest: function () {
        if (Object.isFunction(this.options.onCreate)) {
            var result = this.options.onCreate();
            if (Object.isString(result) && "cancel" === result.toLowerCase()) {
                // only if return is string
                return false;
            }
        }
        this.resolveParameters();
        return true;
    },
    getRequestOptions: function (options, ajaxParam) {
        return Object.extend({
            asynchronous: true,
            method: this.getMethod(),
            evalScripts: true,
            parameters: this.buildParameterString(ajaxParam),
            onFailure: this.options.onFailure,
            onComplete: this.options.onComplete
        }, options || {});
    },
    getAjaxRequest: function (options, ajaxParam) {
        if (!this.initRequest()) {
            return null;
        }
        options = Object.extend({
            onSuccess: (function (response) {
                this.options.parser.load(response);
                this.options.handler(this);
            }).bind(this)
        }, this.getRequestOptions(options, ajaxParam));
        return new Ajax.Request(this.options.baseUrl, options);
    },
    getAjaxUpdater: function (options, ajaxParam) {
        if (!this.initRequest()) {
            return null;
        }
        return new Ajax.Updater(this.options.target,
            this.options.baseUrl, this.getRequestOptions(options, ajaxParam));
    },
    getPeriodicalUpdater: function (xoptions, ajaxParam) {
        if (!this.initRequest()) {
            return null;
        }

        // TODO refactor with closures
        var o = this.options, data = {source: o.source};

        xoptions = Object.extend({
            frequency: o.refreshPeriod
        }, this.getRequestOptions(xoptions, ajaxParam));

        // onComplete is used by API itself don't try to use it
        // cache original onSuccess handler
        data._success = xoptions.onSuccess ? xoptions.onSuccess.bind(this) : Prototype.emptyFunction;
        xoptions.onSuccess = (function () { // inside of onSuccess "this" points to "data"
            if ($(this.source)) {
                this._success(); // call the original onSuccess function
            } else {
                this._updater.stop(); // target lost, stop updater
            }
        }).bind(data);
        data._updater = new Ajax.PeriodicalUpdater(o.target, o.baseUrl, xoptions);
        return data._updater;
    },
    buildParameterString: function (ajaxParam) {
        var params = (this.replaceDefaultParam(ajaxParam) || ''), result = [], key, value, field;
        params.split(',').each(function (pair) {
            pair = pair.split('=');
            key = pair[0].strip();
            if (!key) {
                return;
            }
            key = key + '=';
            value = pair[1];

            field = null;
            if (value) {
                // TODO use id regexp from Prototype ([\w\-\*]+)
                field = value.match(/\{([\w\.:\(\)\[\]]+)\}/);
                if (field && field[1]) {
                    field = $(field[1]);
                }
            }

            if (!field) {
                result.push(key + encodeURIComponent(value));
            } else if (/^(?:checkbox|select-multiple)$/i.test(field.type)) { // BUG 016027
                value = Field.serialize(field);
                if (value) {
                    result.push(value);
                }
            } else if (/^(?:radio|text|textarea|password|hidden|select-one)$/i.test(field.type)) {
                // TODO new HTML5 input types, default input w/o type
                result.push(key + encodeURIComponent(field.value));
            } else {
                result.push(key + encodeURIComponent(field.innerHTML));
            }
        });
        return result.join('&');
    },
    replaceDefaultParam: function (element) {
        var p = this.options.parameters;
        return (element) ? p.replace(AjaxJspTag.DEFAULT_PARAMETER_REGEXP, element.type ? $F(element) : element.innerHTML) : p;
    }
});

/**
 * UpdateField tag.
 */
AjaxJspTag.UpdateField = Class.create(AjaxJspTag.Base, {
    getDefaultOptions: function () {
        return {
            parameters: '',
            valueUpdateByName: false,
            eventType: "click",
            handler: this.handler
        };
    },
    setOptions: function (options) {
        options = Object.extend(this.getDefaultOptions(), options || {});
        options.targets = options.target.split(',');
        // TODO don't use object
        if (options.valueUpdateByName) {
            options.parser = new DefaultResponseParser("xml");
            options.targets.sort(); // O(n log n)
        } else {
            options.parser = new DefaultResponseParser("text");
        }
        this.options = options;
    },
    createListeners: function () {
        this.listener = this.execute.bind(this);
    },
    setListeners: function () {
        var o = this.options, s = $(o.source);
        if (s) {
            s["on" + o.eventType] = this.listener;
        }
    },
    execute: function () {
        this.request = this.getAjaxRequest();
    },
    handler: function (tag) {
        var targets = this.targets, items = this.parser.content, i, len;
        if (this.valueUpdateByName) {
            items = items.sortBy(function (item) {
                return item[0];
            });
            var t = 0, j = 0; // O(n)
            while (t < targets.length && j < items.length) {
                var target = targets[t], item = items[j];
                if (target === item[0]) {
                    $(target).value = item[1];
                    t++;
                    j++;
                } else if (target < item[0]) { // skip target
                    t++;
                } else { // skip item
                    j++;
                }
            }
            /*for (i = 0; i < targets.length; i++) { // O(n^2)
                for (var j = 0; i < items.length; i++) {
                    if (targets[i] === items[j][0]) {
                        $(targets[i]).value = items[j][1];
                        break; // next target
                    }
                }
            }*/
        } else {
            for (i = 0, len = Math.min(targets.length, items.length); i < len; i++) {
                $(targets[i]).value = items[i][0];
            }
        }
    }
});

/**
 * Select tag.
 */
AjaxJspTag.Select = Class.create(AjaxJspTag.Base, {
    initialize: function ($super, options) {
        $super(options);
        if (this.options.executeOnLoad) {
            this.execute();
        }
    },
    getDefaultOptions: function () {
        return {
            parameters: '',
            emptyOptionValue: '',
            emptyOptionName: '',
            defaultOptions: '',
            eventType: "change",
            parser: new DefaultResponseParser("xml"),
            handler: this.handler
        };
    },
    setOptions: function (options) {
        this.options = Object.extend(this.getDefaultOptions(), options || {});
        this.options.defaultOptions = this.options.defaultOptions.split(',');
    },
    createListeners: function () {
        this.listener = this.execute.bind(this);
    },
    setListeners: function () {
        var o = this.options, s = $(o.source);
        if (s) {
            s.ajaxSelect = this.listener;
            s["on" + o.eventType] = this.listener;
        }
    },
    execute: function () {
        this.request = this.getAjaxRequest();
    },
    handler: function () {
        // inside of handler 'this' points to AjaxJspTag.Select#options
        var target = $(this.target), newOption = null;
        if (!target) {
            throw new Error("target lost");
        }
        target.options.length = 0;
        target.disabled = false;
        this.parser.content.each(function (line) {
            newOption = new Option(line[0], line[1]);
            newOption.selected = ((line.length === 3 && "true" === line[2].toLowerCase()) || (this.defaultOptions.indexOf(line[1]) != -1));
            target.options[target.options.length] = newOption;
        }, this);
        if (newOption === null) {
            target.options[target.options.length] = new Option(this.emptyOptionName, this.emptyOptionValue);
            target.disabled = true;
        }
        // auch ein SELECT TAG ?
        // kette ausloessen
        if (Object.isFunction(target.ajaxSelect)) {
            target.ajaxSelect();
        }
    }
});

/**
 * HtmlContent tag.
 */
AjaxJspTag.HtmlContent = Class.create(AjaxJspTag.Base, {
    getDefaultOptions: function () {
        return {
            parameters: '',
            eventType: "click",
            parser: new DefaultResponseParser("html"),
            handler: this.handler
        };
    },
    setEvent: function (element) {
        element["on" + this.options.eventType] = this.listener;
    },
    createListeners: function () {
        this.listener = this.execute.bindAsEventListener(this);
    },
    setListeners: function () {
        var o = this.options;
        if (o.source) {
            this.setEvent($(o.source));
        } else if (o.sourceClass) {
            $("." + o.sourceClass).each(this.setEvent, this);
        }
    },
    execute: function (event) {
        // replace default parameter with value/content of source element
        // event may be undefined if we call execute() manually
        var ajaxParam = this.options.sourceClass ? (event ? Event.element(event) : null) : null;
        this.request = this.getAjaxUpdater(null, ajaxParam);
    }
});

/**
 * Callout tag.
 */
AjaxJspTag.Callout = Class.create(AjaxJspTag.Base, {
    getDefaultOptions: function () {
        return {
            parameters: '',
            overlib: AjaxJspTag.CALLOUT_OVERLIB_DEFAULT,
            parser: new DefaultResponseParser("xmltohtml"),
            openEvent: "mouseover",
            closeEvent: "mouseout",
            handler: this.handler
        };
    },
    setEvent: function (element) {
        element["on" + this.options.openEvent] = this.openListener;
        element["on" + this.options.closeEvent] = this.closeListener;
    },
    createListeners: function () {
        this.openListener = this.calloutOpen.bindAsEventListener(this);
        this.closeListener = this.calloutClose.bindAsEventListener(this);
    },
    setListeners: function () {
        $("." + this.options.sourceClass).each(this.setEvent, this);
    },
    calloutOpen: function (event) {
        this.execute(event);
    },
    calloutClose: function (e) {
        nd(); // TODO make something with overlib's nd()
    },
    execute: function (event) {
        this.request = this.getAjaxRequest(null, Event.element(event));
    },
    handler: function () {
        var c = this.parser.content;
        if (c.strip().length !== 0) { // #4
            var args = [c];
            if (this.title) {
                args.push(CAPTION);
                args.push(this.title);
            }
            if (this.overlib) {
                args.push(this.overlib);
            }
            overlib.apply(this, args);
        }
    }
});

/**
 * TabPanel tag.
 */
AjaxJspTag.TabPanel = Class.create(AjaxJspTag.Base, {
    initialize: function (options) {
        this.setOptions(options);
        this.createElements();
    },
    getDefaultOptions: function () {
        return {
            eventType: "click",
            parser: new DefaultResponseParser("html")
        };
    },
    createElements: function () {
        var o = this.options, defaultTab, that = this;

        this.panel = $(o.id);
        this.panel.down("ul").select("a").zip(o.pages, function (pair) {
            var a = pair[0], tab = pair[1] || {};

            if (tab.parameters) {
                a.parameters = tab.parameters;
            }
            if (tab.defaultTab) {
                defaultTab = defaultTab || a;
            }
            a["on" + o.eventType] = that.execute.bind(that, a);

            return a;
        });

        o.target = o.contentId || this.createContent();
        if (defaultTab) {
            this.execute(defaultTab);
        }
    },
    createContent: function () {
        // create content holder
        var c = new Element("div", {className: "tabContent"});
        this.panel.insert({
            after: c
        });
        return c.identify();
    },
    execute: function (tab) {
        // remove class from any tab
        this.panel.select(".ajaxCurrentTab").invoke("removeClassName", "ajaxCurrentTab");
        // add class to selected tab
        tab.addClass("ajaxCurrentTab");
        // tab.href === web root path + tab.attr("href")
        this.options.baseUrl = tab.attr("href");
        this.options.parameters = tab.parameters;
        this.request = this.getAjaxUpdater(/*{onSuccess: this.handler.bind(this)}*/);
        return false;
    },
    handler: function () {
        // empty
    }
});

/**
 * Autocomplete tag.
 */
AjaxJspTag.XmlToHtmlAutocompleter = Class.create(Ajax.Autocompleter, {
    initialize: function (/*AjaxJspTag.Autocomplete*/autocomplete) {
        this.autocompleteTag = autocomplete;
        var o = autocomplete.options, update = {fake: true, style: {}}; // update = o.divElement
        this.baseInitialize(o.source, update, {
            minChars: o.minChars,
            tokens: o.appendSeparator,
            indicator: o.indicator,
            autoSelect: o.autoSelect,
            paramName: o.paramName,
            evalScripts: true,
            asynchronous: true,
            onComplete: this.onComplete.bind(this),
            afterUpdateElement: function (inputField, selectedItem) {
                autocomplete.handler(selectedItem);
            }
        });
        this.url = o.baseUrl;
    },
    getUpdatedChoices: function ($super) {
        if (!this.autocompleteTag.initRequest()) {
            this.stopIndicator(); // stop ac tag
            return;
        }
        if (this.update.fake) {
            this.update = this.autocompleteTag.createElements(); // lazy creation
        }
        // parse parameters and do replacements
        this.options.defaultParams = this.autocompleteTag.buildParameterString();
        $super(); // Ajax.Autocompleter#getUpdatedChoices()
    },
    onComplete: function (response) {
        var o = this.autocompleteTag.options;
        o.parser.load(response);
        this.updateChoices(o.parser.content);
        if (o.parser.content === null) {
            this.stopIndicator(); // stop ac tag
        }
        // postFunction
        if (Object.isFunction(o.onComplete)) {
            // hier wird nicht base verwendet!!!
            // Disable onupdate event handler of input field
            // because, postFunction can change the content of
            // input field and get into eternal loop.
            var inputf = $(o.source), onupdateHandler = inputf.onupdate;
            inputf.onupdate = '';
            o.onComplete();
            // Enable onupdate event handler of input field
            inputf.onupdate = onupdateHandler;
        }
    }
});

AjaxJspTag.Autocomplete = Class.create(AjaxJspTag.Base, {
    initialize: function (options) {
        this.setOptions(options);
        // this.createElements(); lazy creation
        this.execute();
    },
    getDefaultOptions: function () {
        return {
            parser: new DefaultResponseParser("xmltohtmllist", true)
        };
    },
    setOptions: function (options) {
        this.options = Object.extend(this.getDefaultOptions(), options || {});
        this.options.divElement = "ajaxAuto_" + this.options.source;
    },
    createElements: function () {
        var o = this.options, element = $(o.divElement);
        // remove previous element, if any
        if (element) {
            // element.remove().purge();
            element.purge();
            element.parentNode.removeChild(element);
        }
        element = new Element("div", {id: o.divElement, className: o.className}).hide();
        // insert div at the top of the document so it will not be hidden in case of overflow
        Element.insert(document.body, {top: element});
        return element;
    },
    execute: function () {
        new AjaxJspTag.XmlToHtmlAutocompleter(this);
    },
    handler: function (selectedItem) {
        var o = this.options, target = $(o.target), value = selectedItem.id;
        if (target) {
            if (o.appendSeparator) {
                if (target.value.length > 0) {
                    target.value += o.appendSeparator;
                }
                target.value += value;
            } else {
                target.value = value;
            }
        }
        o.selectedIndex = selectedItem.autocompleteIndex;
        o.selectedObject = selectedItem;
        if (Object.isFunction(o.afterUpdate)) {
            o.afterUpdate(value);
        }
    }
});

/**
 * Portlet tag.
 */
AjaxJspTag.Portlet = Class.create(AjaxJspTag.Base, {
    initialize: function ($super, options) {
        $super(options);
        if (this.options.startMinimize) {
            this.togglePortlet();
        }
        if (this.options.executeOnLoad) {
            this.execute();
        }
    },
    createElements: function () {
        // erstellen des menu um doppelten code zu vermeiden
        var o = this.options, sourceBase = $(o.source).addClass(o.classNamePrefix + "Box");
        if (o.withBar) {
            var bar = new Element("div", {className: o.classNamePrefix + "Tools"});
            this.createButton(bar, "close", o.imageClose);
            this.createButton(bar, "refresh", o.imageRefresh);
            this.createButton(bar, "toggle", o.imageMinimize);
            sourceBase.appendChild(bar);
        }

        var element = new Element("div", {className: o.classNamePrefix + "Title"});
        element.innerHTML = o.title;
        sourceBase.appendChild(element);

        o.target = new Element("div", {className: o.classNamePrefix + "Content"});
        sourceBase.appendChild(o.target);
    },
    createButton: function (bar, name, src) {
        var o = this.options;
        if (o[name]) {
            bar.appendChild(o[name] = new Element("img", {className: o.classNamePrefix + name.capitalize(), src: src}));
        }
    },
    getDefaultOptions: function () {
        return {
            classNamePrefix: "portlet",
            eventType: "click",
            parser: new DefaultResponseParser("html")
        };
    },
    setOptions: function (options) {
        var o = Object.extend(this.getDefaultOptions(), options || {});
        this.options = Object.extend(o, {
            close: o.imageClose && o.source,
            refresh: o.imageRefresh && o.source,
            toggle: o.imageMinimize && o.imageMaximize && o.source,
            // create bar if any image is set
            withBar: o.source && (o.imageClose || o.imageRefresh || (o.imageMinimize && o.imageMaximize))
        });
    },
    createListeners: function () {
        this.closeListener = this.closePortlet.bind(this);
        this.refreshListener = this.refreshPortlet.bind(this);
        this.toggleListener = this.togglePortlet.bind(this);
    },
    setListeners: function () {
        // TODO change to delegate listener on bar[evt]
        var o = this.options, evt = "on" + o.eventType;
        if (o.close) {
            o.close[evt] = this.closeListener;
        }
        if (o.refresh) {
            o.refresh[evt] = this.refreshListener;
        }
        if (o.toggle) {
            o.toggle[evt] = this.toggleListener;
        }
    },
    execute: function () {
        this.ajaxUpdater = this.options.refreshPeriod ? this.getPeriodicalUpdater() : this.getAjaxUpdater();
    },
    stopAutoRefresh: function () {
        // stop auto-update if present
        if (this.ajaxUpdater && this.options.refreshPeriod) {
            this.ajaxUpdater.stop();
        }
        this.ajaxUpdater = null;
    },
    refreshPortlet: function () {
        // clear existing updater
        this.stopAutoRefresh();
        this.execute();
    },
    closePortlet: function () {
        this.stopAutoRefresh();
        Element.remove(this.options.source);
        // TODO save state in cookie
    },
    togglePortlet: function () {
        var o = this.options;
        if (o.toggle) {
            if (o.toggle.src.endsWith(o.imageMinimize)) {
                Element.hide(o.target);
                o.toggle.src = o.imageMaximize;
            } else {
                Element.show(o.target);
                o.toggle.src = o.imageMinimize;
                this.refreshPortlet();
            }
        }
        // TODO save state in cookie
    }
});

/**
 * Tree tag.
 */
AjaxJspTag.Tree = Class.create(AjaxJspTag.Base, {
    initialize: function (options) {
        this.setOptions(options);
        this.execute();
    },
    getDefaultOptions: function () {
        return {
            eventType: "click",
            parser: new ResponseXmlToHtmlLinkListParser(),
            collapsedClass: "collapsedNode",
            expandedClass: "expandedNode",
            treeClass: "tree",
            nodeClass: ''
        };
    },
    execute: function () {
        var t = this.options.target, img;
        if (t) {
            img = $("span_" + t);
            if (img && !this.toggle(img)) {
                $("div_" + t).hide().update();
                return;
            }
        }
        this.request = this.getAjaxRequest({
            onSuccess: this.processResponse.bind(this)
        }, {
            innerHTML: t // request parameter
        });
    },
    toggle: function (e) {
        var o = this.options, expanded = e.hasClass(o.expandedClass);
        e.removeClass(expanded ? o.expandedClass : o.collapsedClass).addClass(expanded ? o.collapsedClass : o.expandedClass);
        return !expanded;
    },
    processResponse: function (response) { // TODO refactor to use default onSuccess in getAjaxRequest?
        var o = this.options;
        o.parser.load({
            responseXML: response.responseXML,
            collapsedClass: o.collapsedClass,
            treeClass: o.treeClass,
            nodeClass: o.nodeClass
        });
        this.handler();
    },
    handler: function () {
        var o = this.options, parser = o.parser, target = $(o.target), displayValue = 'block';
        if (!parser.content) {
            target.innerHTML = "";
            displayValue = 'none';
        }
        target.appendChild(parser.content);
        target.css({
            display: displayValue
        });
        if (displayValue === 'block') {
            target.select("span").each(function (image) {
                image["on" + o.eventType] = this.toggleTreeNode.bind(this, image.id.substring(5));
                //image.bind(o.eventType, this.toggleTreeNode.bind(this, image.id.substring(5)));
            }, this);

            parser.expandedNodes.each(this.toggleTreeNode, this);
            AjaxJspTag.reload();
        }
    },
    toggleTreeNode: function (xid) {
        var opt = Object.clone(this.options);
        opt.target = xid;
        return new AjaxJspTag.Tree(opt);
    }
});

/**
 * Toggle tag.
 */
AjaxJspTag.Toggle = Class.create(AjaxJspTag.Base, {
    getDefaultOptions: function () {
        return {
            parameters: ('rating={' + AjaxJspTag.DEFAULT_PARAMETER + '}'),
            parser: new DefaultResponseParser("text"),
            handler: this.handler
        };
    },
    createElements: function () {
        // create message DIV
        var o = this.options, container = $(o.source);
        if (o.messageClass) {
            // TODO check if $(id) already exists
            this.messageContainer = container.identify() + "_message";
            container.insert({"top": new Element("div", {id: this.messageContainer, className: o.messageClass})});
        }
        this.overClasses = o.selectedOverClass + '|' + o.selectedLessClass + '|' + o.overClass;
        this.allClasses = this.overClasses + '|' + o.selectedClass;
    },
    setEvent: function (element) {
        element.onmouseover = this.mouseoverListener;
        element.onmouseout = this.mouseoutListener;
        element.onclick = this.clickListener;
    },
    createListeners: function () {
        this.mouseoverListener = this.raterMouseOver.bindAsEventListener(this);
        this.mouseoutListener = this.raterMouseOut.bindAsEventListener(this);
        this.clickListener = this.raterClick.bindAsEventListener(this);
    },
    setListeners: function () {
        // TODO use delegate listener on $(this.options.source)
        // attach events to anchors
        this.getAnchors().each(this.setEvent, this);
    },
    setMessage: function (message) {
        if (this.messageContainer) {
            $(this.messageContainer).innerHTML = message;
        }
    },
    getAnchors: function () {
        return $(this.options.source).select('a');
    },
    getSelectedObject: function () {
        return $(this.options.source).select('.' + this.options.selectedClass).pop();
    },
    clearCSSClasses: function (classesRegExp) {
        return this.getAnchors().each(function (element) {
            // remove all class names in single step
            var re = new RegExp("(^|\\s)(" + classesRegExp + ")(?=\\s|$)", "g");
            element.className = element.className.replace(re, ' ').replace(/\s+/g, ' ').strip();
        });
    },
    raterMouseOver: function (e) {
        var o = this.options, i, len;
        // get list of all anchors
        var elements = this.getAnchors();
        // find the current rating
        var selectedObject = this.getSelectedObject();
        var selectedIndex = elements.indexOf(selectedObject);
        // find the index of the 'hovered' element
        var currentIndex = elements.indexOf(Event.element(e));
        this.setMessage(elements[currentIndex].title);
        // iterate over each anchor and apply styles
        for (i = 0, len = elements.length; i < len; i++) {
            if (selectedIndex >= 0 && (i <= selectedIndex || i > currentIndex)) {
                if (i <= selectedIndex) {
                    elements[i].addClass((i <= currentIndex) ? o.selectedOverClass : o.selectedLessClass);
                }
            } else if (i <= currentIndex) {
                elements[i].addClass(o.overClass);
            }
        }
    },
    raterMouseOut: function (e) {
        this.setMessage(''); // clear message
        this.clearCSSClasses(this.overClasses);
    },
    raterClick: function (e) {
        var o = this.options, onoff = $(o.source).hasClass('onoff'), ratingToSend, elements;
        if (onoff) {
            var selectedObject = this.getSelectedObject();
            // get list of all anchors (single anchor for onoff)
            elements = this.clearCSSClasses(this.allClasses);
            // update styles
            if (!selectedObject) {
                elements[0].addClass(o.selectedClass);
            }
            // prepare request parameters
            // send opposite of what was selected
            var ratings = o.ratings.split(',');
            ratingToSend = (elements[0].title == ratings[0]) ? ratings[1] : ratings[0];
            elements[0].title = ratingToSend;
        } else {
            // get list of all anchors
            elements = this.clearCSSClasses(this.allClasses);
            // find the index of the 'hovered' element
            var currentIndex = elements.indexOf(Event.element(e));
            // update styles
            for (var i = 0; i <= currentIndex; i++) {
                elements[i].addClass(o.selectedClass);
            }
            // prepare request parameters
            ratingToSend = elements[currentIndex].title;
        }
        // send request
        this.execute({
            innerHTML: ratingToSend
        }); // warp this to make replacement valid!
        // set field (if defined)
        if (o.state) {
            $(o.state).value = ratingToSend;
        }
    },
    execute: function (ratingValue) {
        this.request = this.getAjaxRequest(null, ratingValue);
    },
    handler: function () {
        // daten in items
        var result = this.parser.content[0][0]; // on/off / 1,2,3
        if (Object.isFunction(this.updateFunction)) {
            this.updateFunction(result); // ??? XXX do we need this!
        }
    }
});

/**
 * OnClick tag.
 */
AjaxJspTag.OnClick = Class.create(AjaxJspTag.Base, {
    initialize: function (options) {
        this.setOptions(options);
        this.execute();
    },
    execute: function () {
        this.request = this.getAjaxUpdater({
            requestHeaders: this.options.requestHeaders,
            onSuccess: this.handler.bind(this)
        }, this.options.eventBase);
    },
    handler: function () {
        // etwas machen wenn erfolgreich?
    }
});

/**
 * Submit tag.
 */
AjaxJspTag.Submit = Class.create(AjaxJspTag.Base, {
    // TODO option for multiple submit buttons: serialize(true, {hash: false, submit: ?})
    createListeners: function () {
        this.listener = this.execute.bind(this);
    },
    setListeners: function () {
        var form = $(this.options.source);
        if (form) {
            form.onsubmit = this.listener;
        }
    },
    execute: function () {
        try {
            var o = this.options, form = $(o.source);
            if (form) {
                o.baseUrl = form.action;
                this.request = this.getAjaxUpdater({
                    parameters: form.serialize(true)
                });
            }
        } catch (e) {
            alert("Exception in form.onsubmit: " + e.message);
        } finally {
            // prevent form submission
            return false;
        }
    }
});
