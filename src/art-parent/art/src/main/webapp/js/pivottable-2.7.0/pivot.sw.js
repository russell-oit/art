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
    var c3r, d3r, frFmt, frFmtInt, frFmtPct, gcr, nf, r, tpl;
    nf = $.pivotUtilities.numberFormat;
    tpl = $.pivotUtilities.aggregatorTemplates;
    r = $.pivotUtilities.renderers;
    gcr = $.pivotUtilities.gchart_renderers;
    d3r = $.pivotUtilities.d3_renderers;
    c3r = $.pivotUtilities.c3_renderers;
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
          filterResults: "Chuja",
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
        "80% Upper Bound": tpl.sumOverSumBound80(true, frFmt),
        "80% Lower Bound": tpl.sumOverSumBound80(false, frFmt),
        "Sum as Fraction of Total": tpl.fractionOf(tpl.sum(), "total", frFmtPct),
        "Jumla kama sehemu ya safu": tpl.fractionOf(tpl.sum(), "row", frFmtPct),
        "Jumla kama sehemu ya safu-wima": tpl.fractionOf(tpl.sum(), "col", frFmtPct),
        "Count as Fraction of Total": tpl.fractionOf(tpl.count(), "total", frFmtPct),
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
        "Chati mstari": c3r["Line Chart"],
        "Chati pau": c3r["Bar Chart"],
        "Upau mpororo": c3r["Stacked Bar Chart"],
        "Chati eneo": c3r["Area Chart"],
		"Chati tawanya": c3r["Scatter Chart"]
      };
    }
    return $.pivotUtilities.locales.sw;
  });

}).call(this);
