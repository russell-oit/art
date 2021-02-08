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
    return $.pivotUtilities.locales.it = {
      localeStrings: {
        renderError: "Si è verificato un errore durante la creazione della tabella.",
        computeError: "Si è verificato un errore di calcolo nella tabella.",
        uiRenderError: "Si è verificato un errore durante il disegno di interfaccia della tabella pivot.",
        selectAll: "Seleziona tutto",
        selectNone: "Deseleziona tutto",
        tooMany: "(troppi valori da visualizzare)",
        filterResults: "Filtra i valori",
        apply: "Applica",
        cancel: "Annulla",
        totals: "Totali",
        vs: "su",
        by: "da"
      },
      aggregators: {
        "Numero": tpl.count(frFmtInt),
        "Numero di valori unici": tpl.countUnique(frFmtInt),
        "Elenco di valori unici": tpl.listUnique(", "),
        "Somma": tpl.sum(frFmt),
        "Somma intera": tpl.sum(frFmtInt),
        "Media": tpl.average(frFmt),
        "Minimo": tpl.min(frFmt),
        "Massimo": tpl.max(frFmt),
		"Inizio": tpl.first(frFmt),
        "Fine": tpl.last(frFmt),
        "Rapporto": tpl.sumOverSum(frFmt),
        "Limite superiore 80%": tpl.sumOverSumBound80(true, frFmt),
        "Limite inferiore 80%": tpl.sumOverSumBound80(false, frFmt),
        "Somma proporzionale al totale": tpl.fractionOf(tpl.sum(), "total", frFmtPct),
        "Somma proporzionale alla riga": tpl.fractionOf(tpl.sum(), "row", frFmtPct),
        "Somma proporzionale alla colonna": tpl.fractionOf(tpl.sum(), "col", frFmtPct),
        "Numero proporzionale al totale": tpl.fractionOf(tpl.count(), "total", frFmtPct),
        "Numero proporzionale alla riga": tpl.fractionOf(tpl.count(), "row", frFmtPct),
        "Numero proporzionale alla colonna": tpl.fractionOf(tpl.count(), "col", frFmtPct)
      },
      renderers: {
        "Tabella": $.pivotUtilities.renderers["Table"],
        "Tabella con grafico": $.pivotUtilities.renderers["Table Barchart"],
        "Mappa di calore": $.pivotUtilities.renderers["Heatmap"],
        "Mappa di calore per righe": $.pivotUtilities.renderers["Row Heatmap"],
        "Mappa di calore per colonne": $.pivotUtilities.renderers["Col Heatmap"]
      }
    };
	if (gcr) {
      $.pivotUtilities.locales.it.gchart_renderers = {
        "Line Chart": gcr["Line Chart"],
        "Bar Chart": gcr["Bar Chart"],
        "Stacked Bar Chart": gcr["Stacked Bar Chart"],
        "Area Chart": gcr["Area Chart"],
		"Scatter Chart": gcr["Scatter Chart"]
      };
    }
    if (d3r) {
      $.pivotUtilities.locales.it.d3_renderers = {
        "Treemap": d3r["Treemap"]
      };
    }
    if (c3r) {
      $.pivotUtilities.locales.it.c3_renderers = {
        "Grafico A Linea C3": c3r["Line Chart"],
        "Grafico A Barre C3": c3r["Bar Chart"],
        "Grafico A Barre Impilate C3": c3r["Stacked Bar Chart"],
        "Grafico Dell’Area C3": c3r["Area Chart"],
		"C3 Grafico A Barre Orizzontali": c3r["Horizontal Bar Chart"],
		"C3 Grafico A Barre Impilate Orizzontale": c3r["Horizontal Stacked Bar Chart"],
		"Grafico A Scatter C3": c3r["Scatter Chart"]
      };
    }
	if (plr) {
      $.pivotUtilities.locales.it.plotly_renderers = {
        "Grafico A Linea Interattivo": plr["Line Chart"],
        "Grafico A Barre Interattivo": plr["Bar Chart"],
        "Grafico A Barre Impilate Interattivo": plr["Stacked Bar Chart"],
		"Grafico Area Interattivo": plr["Area Chart"],
		"Grafico A Barre Orizzontale Interattivo": plr["Horizontal Bar Chart"],
		"Grafico A Barre Impilate Orizzontale Interattivo": plr["Horizontal Stacked Bar Chart"],
		"Grafico A Scatter Interattivo": plr["Scatter Chart"],
		"Grafico A Torta Multipla Interattivo": plr["Multiple Pie Chart"]
      };
    }
	if (er) {
		$.pivotUtilities.locales.it.export_renderers = {
			"Esportazione TSV" : er["TSV Export"]
		};
	}
	if (sr) {
		$.pivotUtilities.locales.it.subtotal_renderers = {
			"Tabella Con Subtotale" : sr["Table With Subtotal"],
			"Tabella Con Grafico A Barre Subtotale" : sr["Table With Subtotal Bar Chart"],
			"Tabella Con Subtotale Mappa di calore" : sr["Table With Subtotal Heatmap"],
			"Tabella Con Subtotale Riga Mappa di calore" : sr["Table With Subtotal Row Heatmap"],
			"Tabella Con Subtotale Colonna Mappa di calore" : sr["Table With Subtotal Col Heatmap"]
		};
	}
    return $.pivotUtilities.locales.it;
  });

}).call(this);
