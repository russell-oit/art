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
	//replace $.pivotUtilities.locales.en with $.pivotUtilities.locales.<locale>
    $.pivotUtilities.locales.en = {
      localeStrings: {
          renderError: "An error occurred rendering the PivotTable results.",
          computeError: "An error occurred computing the PivotTable results.",
          uiRenderError: "An error occurred rendering the PivotTable UI.",
          selectAll: "Select All",
          selectNone: "Select None",
          tooMany: "(too many to list)",
          filterResults: "Filter values",
          apply: "Apply",
          cancel: "Cancel",
          totals: "Totals",
          vs: "vs",
          by: "by"
      },
      aggregators: {
		"Count": tpl.count(frFmtInt),
        "Count Unique Values": tpl.countUnique(frFmtInt),
        "List Unique Values": tpl.listUnique(", "),
        "Sum": tpl.sum(frFmt),
        "Integer Sum": tpl.sum(frFmtInt),
        "Average": tpl.average(frFmt),
        "Minimum": tpl.min(frFmt),
        "Maximum": tpl.max(frFmt),
        "First": tpl.first(frFmt),
        "Last": tpl.last(frFmt),
        "Sum over Sum": tpl.sumOverSum(frFmt),
        "80% Upper Bound": tpl.sumOverSumBound80(true, frFmt),
        "80% Lower Bound": tpl.sumOverSumBound80(false, frFmt),
        "Sum as Fraction of Total": tpl.fractionOf(tpl.sum(), "total", frFmtPct),
        "Sum as Fraction of Rows": tpl.fractionOf(tpl.sum(), "row", frFmtPct),
        "Sum as Fraction of Columns": tpl.fractionOf(tpl.sum(), "col", frFmtPct),
        "Count as Fraction of Total": tpl.fractionOf(tpl.count(), "total", frFmtPct),
        "Count as Fraction of Rows": tpl.fractionOf(tpl.count(), "row", frFmtPct),
        "Count as Fraction of Columns": tpl.fractionOf(tpl.count(), "col", frFmtPct)
      },
      renderers: {
        "Table": r["Table"],
        "Table Barchart": r["Table Barchart"],
        "Heatmap": r["Heatmap"],
        "Row Heatmap": r["Row Heatmap"],
        "Col Heatmap": r["Col Heatmap"]
      }
    };
    if (gcr) {
      $.pivotUtilities.locales.en.gchart_renderers = {
        "Line Chart": gcr["Line Chart"],
        "Bar Chart": gcr["Bar Chart"],
        "Stacked Bar Chart": gcr["Stacked Bar Chart"],
        "Area Chart": gcr["Area Chart"],
		"Scatter Chart": gcr["Scatter Chart"]
      };
    }
    if (d3r) {
      $.pivotUtilities.locales.en.d3_renderers = {
        "Treemap": d3r["Treemap"]
      };
    }
    if (c3r) {
      $.pivotUtilities.locales.en.c3_renderers = {
        "Line Chart": c3r["Line Chart"],
        "Bar Chart": c3r["Bar Chart"],
        "Stacked Bar Chart": c3r["Stacked Bar Chart"],
        "Area Chart": c3r["Area Chart"],
		"Scatter Chart": c3r["Scatter Chart"]
      };
    }
    return $.pivotUtilities.locales.en;
  });

}).call(this);
