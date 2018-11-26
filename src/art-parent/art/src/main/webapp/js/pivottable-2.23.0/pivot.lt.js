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
			decimalSep: "."
		});
		frFmtInt = nf({
			digitsAfterDecimal: 0,
			thousandsSep: " ",
			decimalSep: "."
		});
		frFmtPct = nf({
			digitsAfterDecimal: 2,
			scaler: 100,
			suffix: "%",
			thousandsSep: " ",
			decimalSep: "."
		});
		$.pivotUtilities.locales.lt = {
			localeStrings: {
				renderError: "Įvyko klaida generuojant suvestinių (Pivot) lentelės rezultatus.",
				computeError: "Įvyko klaida skaičiuojant suvestinių (Pivot) lentelės rezultatus.",
				uiRenderError: "Įvyko klaida generuojant suvestinių (Pivot) lentelės UI.",
				selectAll: "Pažymėti viską",
				selectNone: "Panaikinti žymėjimą",
				tooMany: "(per daug, kad pateikti)",
				filterResults: "Filtruoti rezultatus",
				apply: "Pritaikyti",
				cancel: "Atsisakyti",
				totals: "Suminiai duomenys",
				vs: "prieš",
				by: "pagal"
			},
			aggregators: {
				"Kiekis": tpl.count(frFmtInt),
				"Unikalių reikšmių kiekis": tpl.countUnique(frFmtInt),
				"Unikalių reikšmių sąrašas": tpl.listUnique(", "),
				"Suma": tpl.sum(frFmt),
				"Sveikųjų skaičių suma": tpl.sum(frFmtInt),
				"Vidurkis": tpl.average(frFmt),
				"Minimumas": tpl.min(frFmt),
				"Maksimumas": tpl.max(frFmt),
				"Pirmas": tpl.first(frFmt),
				"Paskutinis": tpl.last(frFmt),
				"Sumų suma": tpl.sumOverSum(frFmt),
				"80% viršutinės ribos": tpl.sumOverSumBound80(true, frFmt),
				"80% apatinės ribos": tpl.sumOverSumBound80(false, frFmt),
				"Suma procentais nuo viso kiekio": tpl.fractionOf(tpl.sum(), "total", frFmtPct),
				"Suma procentais pagal eilutes": tpl.fractionOf(tpl.sum(), "row", frFmtPct),
				"Suma procentais pagal stulpelius": tpl.fractionOf(tpl.sum(), "col", frFmtPct),
				"Kiekis procentais nuo viso kiekio": tpl.fractionOf(tpl.count(), "total", frFmtPct),
				"Kiekis procentais pagal eilutes": tpl.fractionOf(tpl.count(), "row", frFmtPct),
				"Kiekis procentais pagal stulpelius": tpl.fractionOf(tpl.count(), "col", frFmtPct)
			},
			renderers: {
				"Lentelė": r["Table"],
				"Lentelė su juostine diagrama": r["Table Barchart"],
				"Temperatūrų zonų žemėlapis": r["Heatmap"],
				"Temperatūrų zonų žemėlapis linijomis": r["Row Heatmap"],
				"Temperatūrų zonų žemėlapis stulpeliais": r["Col Heatmap"]
			}
		};
		if (gcr) {
			$.pivotUtilities.locales.lt.gchart_renderers = {
				"Linijinė diagrama": gcr["Line Chart"],
				"Juostinė diagrama": gcr["Bar Chart"],
				"Sudurtinė juostinė diagrama": gcr["Stacked Bar Chart"],
				"Sritinė diagrama": gcr["Area Chart"],
				"Taškinė diagrama": gcr["Scatter Chart"]
			};
		}
		if (d3r) {
			$.pivotUtilities.locales.lt.d3_renderers = {
				"Hierarchinė diagrama": d3r["Treemap"]
			};
		}
		if (c3r) {
			$.pivotUtilities.locales.lt.c3_renderers = {
				"Linijinė diagrama C3": c3r["Line Chart"],
				"Juostinė diagrama C3": c3r["Bar Chart"],
				"Sudurtinė juostinė diagrama C3": c3r["Stacked Bar Chart"],
				"Sritinė diagrama C3": c3r["Area Chart"],
				"C3 Horizontal Bar Chart": c3r["Horizontal Bar Chart"],
				"C3 Horizontal Stacked Bar Chart": c3r["Horizontal Stacked Bar Chart"],
				"Taškinė diagrama C3": c3r["Scatter Chart"]
			};
		}
		if (plr) {
			$.pivotUtilities.locales.lt.plotly_renderers = {
				"Linijinė diagrama Plotly": plr["Line Chart"],
				"Juostinė diagrama Plotly": plr["Bar Chart"],
				"Sudurtinė juostinė diagrama Plotly": plr["Stacked Bar Chart"],
				"Sritinė diagrama Plotly": plr["Area Chart"],
				"Plotly Horizontal Bar Chart": plr["Horizontal Bar Chart"],
				"Plotly Horizontal Stacked Bar Chart": plr["Horizontal Stacked Bar Chart"],
				"Taškinė diagrama Plotly": plr["Scatter Chart"]
			};
		}
		if (er) {
			$.pivotUtilities.locales.lt.export_renderers = {
				"TSV Export" : er["TSV Export"]
			};
		}
		if (sr) {
			$.pivotUtilities.locales.lt.subtotal_renderers = {
				"Table With Subtotal" : sr["Table With Subtotal"],
				"Table With Subtotal Bar Chart" : sr["Table With Subtotal Bar Chart"],
				"Table With Subtotal Heatmap" : sr["Table With Subtotal Heatmap"],
				"Table With Subtotal Row Heatmap" : sr["Table With Subtotal Row Heatmap"],
				"Table With Subtotal Col Heatmap" : sr["Table With Subtotal Col Heatmap"]
			};
		}
		return $.pivotUtilities.locales.lt;
	});

}).call(this);
