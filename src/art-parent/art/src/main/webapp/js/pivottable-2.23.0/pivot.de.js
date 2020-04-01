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
      thousandsSep: " ",
      decimalSep: ","
    });
    frFmtInt = nf({
      digitsAfterDecimal: 0,
      thousandsSep: " ",
      decimalSep: ","
    });
    frFmtPct = nf({
      digitsAfterDecimal: 1,
      scaler: 100,
      suffix: "%",
      thousandsSep: " ",
      decimalSep: ","
    });
    $.pivotUtilities.locales.de = {
      localeStrings: {
        renderError: "Bei der Darstellung der Pivot-Tabelle ist ein Fehler aufgetreten.",
        computeError: "Bei der Berechnung der Pivot-Tabelle ist ein Fehler aufgetreten.",
        uiRenderError: "Bei der Darstellung Oberfläche der Pivot-Tabelle ist ein Fehler aufgetreten.",
        selectAll: "Alles auswählen",
        selectNone: "Nichts auswählen",
        tooMany: "(zu viele für Liste)",
        filterResults: "Ergebnisse filtern",
		apply: "Anwenden",
		cancel: "Abbrechen",
		totals: "Gesamt",
        vs: "gegen",
        by: "pro"
      },
      aggregators: {
        "Anzahl": tpl.count(frFmtInt),
        "Anzahl eindeutiger Werte": tpl.countUnique(frFmtInt),
        "Liste eindeutiger Werte": tpl.listUnique(", "),
        "Summe": tpl.sum(frFmt),
        "Ganzzahlige Summe": tpl.sum(frFmtInt),
        "Durchschnitt": tpl.average(frFmt),
        "Minimum": tpl.min(frFmt),
        "Maximum": tpl.max(frFmt),
		"Erster": tpl.first(frFmt),
        "Letzter": tpl.last(frFmt),
        "Summe über Summe": tpl.sumOverSum(frFmt),
        "80% Obergrenze": tpl.sumOverSumBound80(true, frFmt),
        "80% Untergrenze": tpl.sumOverSumBound80(false, frFmt),
        "Summe als Anteil von Gesamt": tpl.fractionOf(tpl.sum(), "total", frFmtPct),
        "Summe als Anteil von Zeile": tpl.fractionOf(tpl.sum(), "row", frFmtPct),
        "Summe als Anteil von Spalte": tpl.fractionOf(tpl.sum(), "col", frFmtPct),
        "Anzahl als Anteil von Gesamt": tpl.fractionOf(tpl.count(), "total", frFmtPct),
        "Anzahl als Anteil von Zeile": tpl.fractionOf(tpl.count(), "row", frFmtPct),
        "Anzahl als Anteil von Spalte": tpl.fractionOf(tpl.count(), "col", frFmtPct)
      },
      renderers: {
        "Tabelle": $.pivotUtilities.renderers["Table"],
        "Tabelle mit Balkendiagramm": $.pivotUtilities.renderers["Table Barchart"],
        "Heatmap": $.pivotUtilities.renderers["Heatmap"],
        "Heatmap pro Zeile": $.pivotUtilities.renderers["Row Heatmap"],
        "Heatmap pro Spalte": $.pivotUtilities.renderers["Col Heatmap"]
      }
    };
	if (gcr) {
      $.pivotUtilities.locales.de.gchart_renderers = {
        "Line Chart": gcr["Line Chart"],
        "Bar Chart": gcr["Bar Chart"],
        "Stacked Bar Chart": gcr["Stacked Bar Chart"],
        "Area Chart": gcr["Area Chart"],
		"Scatter Chart": gcr["Scatter Chart"]
      };
    }
    if (d3r) {
      $.pivotUtilities.locales.de.d3_renderers = {
        "Treemap": d3r["Treemap"]
      };
    }
    if (c3r) {
      $.pivotUtilities.locales.de.c3_renderers = {
        "C3 Liniendiagramm": c3r["Line Chart"],
        "C3 Balkendiagramm": c3r["Bar Chart"],
        "C3 gestapeltes Balkendiagramm": c3r["Stacked Bar Chart"],
        "C3 Flächenduagramm": c3r["Area Chart"],
		"C3 horizontales Balkendiagramm": c3r["Horizontal Bar Chart"],
		"C3 horizontal gestapeltes Balkendiagramm": c3r["Horizontal Stacked Bar Chart"],
		"Verteilungsdiagramm": c3r["Scatter Chart"]
      };
    }
	if (plr) {
      $.pivotUtilities.locales.de.plotly_renderers = {
        "Plotly Line Chart": plr["Line Chart"],
        "Plotly Bar Chart": plr["Bar Chart"],
        "Plotly Stacked Bar Chart": plr["Stacked Bar Chart"],
		"Plotly Area Chart": plr["Area Chart"],
		"Plotly Horizontal Bar Chart": plr["Horizontal Bar Chart"],
		"Plotly Horizontal Stacked Bar Chart": plr["Horizontal Stacked Bar Chart"],
		"Plotly Scatter Chart": plr["Scatter Chart"],
		"Plotly Multiple Pie Chart": plr["Multiple Pie Chart"]
      };
    }
	if (er) {
		$.pivotUtilities.locales.de.export_renderers = {
			"TSV Export" : er["TSV Export"]
		};
	}
	if (sr) {
		$.pivotUtilities.locales.de.subtotal_renderers = {
			"Table With Subtotal" : sr["Table With Subtotal"],
			"Table With Subtotal Bar Chart" : sr["Table With Subtotal Bar Chart"],
			"Table With Subtotal Heatmap" : sr["Table With Subtotal Heatmap"],
			"Table With Subtotal Row Heatmap" : sr["Table With Subtotal Row Heatmap"],
			"Table With Subtotal Col Heatmap" : sr["Table With Subtotal Col Heatmap"]
		};
	}
	return $.pivotUtilities.locales.de;
  });

}).call(this);

//# sourceMappingURL=pivot.de.js.map
