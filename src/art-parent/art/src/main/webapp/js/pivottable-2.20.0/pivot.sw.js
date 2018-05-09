(function() {
  var callWithJQuery;

  callWithJQuery = function(pivotModule) {
    if (typeof exports === "object" && typeof module === "object") {
      return pivotModule(require("jquery"));
    } else if (typeof define === "function" && define.amd) {
      return define(["jquery"], pivotModule);
    } else {
      return pivotModule(jQuery);
    }
  };

  callWithJQuery(function($) {
    var c3r, d3r, frFmt, frFmtInt, frFmtPct, gcr, nf, r, tpl, plr, er, sr;
    nf = $.pivotUtilities.numberFormat;
    tpl = $.pivotUtilities.aggregatorTemplates;
    r = $.pivotUtilities.renderers;
    gcr = $.pivotUtilities.gchart_renderers;
    d3r = $.pivotUtilities.d3_renderers;
    c3r = $.pivotUtilities.c3_renderers;
	plr = $.pivotUtilities.plotly_renderers;
	er = $.pivotUtilities.export_renderers;
	sr = $.pivotUtilities.subtotal_renderers;
    frFmt = nf({
      thousandsSep: ",",
      decimalSep: "."
    });
    frFmtInt = nf({
      digitsAfterDecimal: 0,
      thousandsSep: ",",
      decimalSep: "."
    });
    frFmtPct = nf({
      digitsAfterDecimal: 2,
      scaler: 100,
      suffix: "%",
      thousandsSep: ",",
      decimalSep: "."
    });
    $.pivotUtilities.locales.sw = {
      localeStrings: {
          renderError: "Kulikuwa na hitilafu matokeo yakionyeshwa.",
          computeError: "Kulikuwa na hitilafu matokeo yakiidadishwa.",
          uiRenderError: "Kulikiwa na hitilafu PivotTable ikionyeshwa.",
          selectAll: "Chagua yote",
          selectNone: "Ondoa yote",
          tooMany: "(nyingi sana kuorodhesha)",
          filterResults: "Chuja thamani",
          apply: "Tekeleza",
          cancel: "Ghairi",
          totals: "Idadi",
          vs: "vs",
          by: "by"
      },
      aggregators: {
		"Idadi": tpl.count(frFmtInt),
        "Idadi ya thamani-kipekee": tpl.countUnique(frFmtInt),
        "Orodhesha thamani-kipekee": tpl.listUnique(", "),
        "Jumla": tpl.sum(frFmt),
        "Jumla (namba kamili)": tpl.sum(frFmtInt),
        "Wastani": tpl.average(frFmt),
        "Ndogo zaidi": tpl.min(frFmt),
        "Kubwa zaidi": tpl.max(frFmt),
        "Kwanza": tpl.first(frFmt),
        "Mwisho": tpl.last(frFmt),
        "Jumla kwa jumla": tpl.sumOverSum(frFmt),
        "80% kiwango cha juu": tpl.sumOverSumBound80(true, frFmt),
        "80% kiwango cha chini": tpl.sumOverSumBound80(false, frFmt),
        "Jumla kama sehemu ya jumla": tpl.fractionOf(tpl.sum(), "total", frFmtPct),
        "Jumla kama sehemu ya safu": tpl.fractionOf(tpl.sum(), "row", frFmtPct),
        "Jumla kama sehemu ya safu-wima": tpl.fractionOf(tpl.sum(), "col", frFmtPct),
        "Idadi kama sehemu ya jumla": tpl.fractionOf(tpl.count(), "total", frFmtPct),
        "Idadi kama sehemu ya safu": tpl.fractionOf(tpl.count(), "row", frFmtPct),
        "Idadi kama sehemu ya safu-wima": tpl.fractionOf(tpl.count(), "col", frFmtPct)
      },
      renderers: {
        "Meza": r["Table"],
        "Meza yenye chati pau": r["Table Barchart"],
        "Ramani-rangi": r["Heatmap"],
        "Ramani-rangi ya safu": r["Row Heatmap"],
        "Ramani-rangi ya safu-wima": r["Col Heatmap"]
      }
    };
    if (gcr) {
      $.pivotUtilities.locales.sw.gchart_renderers = {
        "Chati mstari": gcr["Line Chart"],
        "Chati pau": gcr["Bar Chart"],
        "Upau mpororo": gcr["Stacked Bar Chart"],
        "Chati eneo": gcr["Area Chart"],
		"Chati tawanya": gcr["Scatter Chart"]
      };
    }
    if (d3r) {
      $.pivotUtilities.locales.sw.d3_renderers = {
        "Ramani-mti": d3r["Treemap"]
      };
    }
    if (c3r) {
      $.pivotUtilities.locales.sw.c3_renderers = {
        "Chati mstari C3": c3r["Line Chart"],
        "Chati pau C3": c3r["Bar Chart"],
        "Upau mpororo C3": c3r["Stacked Bar Chart"],
        "Chati eneo C3": c3r["Area Chart"],
		"Chati pau mlalo C3": c3r["Horizontal Bar Chart"],
		"Upau mpororo mlalo C3": c3r["Horizontal Stacked Bar Chart"],
		"Chati tawanya C3": c3r["Scatter Chart"]
      };
    }
	if (plr) {
      $.pivotUtilities.locales.sw.plotly_renderers = {
        "Chati mstari Plotly": plr["Line Chart"],
        "Chati pau Plotly": plr["Bar Chart"],
        "Upau mpororo Plotly": plr["Stacked Bar Chart"],
		"Chati pau mlalo Plotly": plr["Horizontal Bar Chart"],
		"Upau mpororo mlalo Plotly": plr["Horizontal Stacked Bar Chart"],
		"Chati tawanya Plotly": plr["Scatter Chart"]
      };
    }
	if (er) {
		$.pivotUtilities.locales.sw.export_renderers = {
			"Hamishia TSV" : er["TSV Export"]
		};
	}
	if (sr) {
		$.pivotUtilities.locales.sw.subtotal_renderers = {
			"Meza na Subtotal" : sr["Table With Subtotal"],
			"Meza na Subtotal Bar Chart" : sr["Table With Subtotal Bar Chart"],
			"Meza na Subtotal Heatmap" : sr["Table With Subtotal Heatmap"],
			"Meza na Subtotal Row Heatmap" : sr["Table With Subtotal Row Heatmap"],
			"Meza na Subtotal Col Heatmap" : sr["Table With Subtotal Col Heatmap"]
		};
	}
    return $.pivotUtilities.locales.sw;
  });

}).call(this);
