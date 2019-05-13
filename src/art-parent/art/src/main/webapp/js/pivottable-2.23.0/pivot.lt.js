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
				"Eilutės temperatūrų zonų žemėlapis": r["Row Heatmap"],
				"Stulpelio temperatūrų zonų žemėlapis": r["Col Heatmap"]
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
				"C3 Linijinė diagrama": c3r["Line Chart"],
				"C3 Juostinė diagrama": c3r["Bar Chart"],
				"C3 Sudurtinė juostinė diagrama": c3r["Stacked Bar Chart"],
				"C3 Sritinė diagrama": c3r["Area Chart"],
				"C3 Horizontali juostinė diagrama": c3r["Horizontal Bar Chart"],
				"C3 Horizontali sudurtinė juostinė diagrama": c3r["Horizontal Stacked Bar Chart"],
				"C3 Taškinė diagrama": c3r["Scatter Chart"]
			};
		}
		if (plr) {
			$.pivotUtilities.locales.lt.plotly_renderers = {
				"„Plotly“ Linijinė diagrama": plr["Line Chart"],
				"„Plotly“ Juostinė diagrama": plr["Bar Chart"],
				"„Plotly“ Sudurtinė juostinė diagrama": plr["Stacked Bar Chart"],
				"„Plotly“ Sritinė diagrama": plr["Area Chart"],
				"„Plotly“ Horizontali juostinė diagrama": plr["Horizontal Bar Chart"],
				"„Plotly“ Horizontali sudurtinė juostinė diagrama": plr["Horizontal Stacked Bar Chart"],
				"„Plotly“ Taškinė diagrama": plr["Scatter Chart"],
				"„Plotly“ Daugiaskritulinė diagrama": plr["Multiple Pie Chart"]
			};
		}
		if (er) {
			$.pivotUtilities.locales.lt.export_renderers = {
				"TSV eksportas" : er["TSV Export"]
			};
		}
		if (sr) {
			$.pivotUtilities.locales.lt.subtotal_renderers = {
				"Lentelė su tarpine suma" : sr["Table With Subtotal"],
				"Lentelė su tarpine juostine diagrama" : sr["Table With Subtotal Bar Chart"],
				"Lentelė su tarpiniu temperatūrų zonų žemėlapiu" : sr["Table With Subtotal Heatmap"],
				"Lentelė su tarpiniu eilutės temperatūrų zonų žemėlapiu" : sr["Table With Subtotal Row Heatmap"],
				"Lentelė su tarpiniu stulpelio temperatūrų zonų žemėlapiu" : sr["Table With Subtotal Col Heatmap"]
			};
		}
		return $.pivotUtilities.locales.lt;
	});

}).call(this);
