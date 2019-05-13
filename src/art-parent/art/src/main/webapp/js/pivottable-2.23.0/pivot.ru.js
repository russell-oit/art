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
    var frFmt, frFmtInt, frFmtPct, nf, tpl, c3r, d3r, gcr, r, plr, er, sr;
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
    return $.pivotUtilities.locales.ru = {
      localeStrings: {
        renderError: "Ошибка рендеринга страницы.",
        computeError: "Ошибка табличных расчетов.",
        uiRenderError: "Ошибка во время прорисовки и динамического расчета таблицы.",
        selectAll: "Выбрать все",
        selectNone: "Снять выделение",
        tooMany: "(Выбрано слишком много значений)",
        filterResults: "Возможные значения",
		apply: "Применить",
		cancel: "Отмена",
        totals: "Всего",
        vs: "на",
        by: "с"
      },
      aggregators: {
        "Кол-во": tpl.count(frFmtInt),
        "Кол-во уникальных": tpl.countUnique(frFmtInt),
        "Список уникальных": tpl.listUnique(", "),
        "Сумма": tpl.sum(frFmt),
        "Сумма целых": tpl.sum(frFmtInt),
        "Среднее": tpl.average(frFmt),
        "Минимум": tpl.min(frFmt),
        "Максимум": tpl.max(frFmt),
        "Сумма по сумме": tpl.sumOverSum(frFmt),
        "80% верхней границы": tpl.sumOverSumBound80(true, frFmt),
        "80% нижней границы": tpl.sumOverSumBound80(false, frFmt),
        "Доля по всему": tpl.fractionOf(tpl.sum(), "total", frFmtPct),
        "Доля по строке": tpl.fractionOf(tpl.sum(), "row", frFmtPct),
        "Доля по столбцу": tpl.fractionOf(tpl.sum(), "col", frFmtPct),
        "Кол-во по всему": tpl.fractionOf(tpl.count(), "total", frFmtPct),
        "Кол-во по строке": tpl.fractionOf(tpl.count(), "row", frFmtPct),
        "Кол-во по столбцу": tpl.fractionOf(tpl.count(), "col", frFmtPct)
      },
      renderers: {
        "Таблица": $.pivotUtilities.renderers["Table"],
        "График столбцы": $.pivotUtilities.renderers["Table Barchart"],
        "Тепловая карта": $.pivotUtilities.renderers["Heatmap"],
        "Тепловая карта по строке": $.pivotUtilities.renderers["Row Heatmap"],
        "Тепловая карта по столбцу": $.pivotUtilities.renderers["Col Heatmap"]
      }
    };
	if (gcr) {
       $.pivotUtilities.locales.ru.gchart_renderers = {
        "Line Chart": gcr["Line Chart"],
        "Bar Chart": gcr["Bar Chart"],
        "Stacked Bar Chart": gcr["Stacked Bar Chart"],
        "Area Chart": gcr["Area Chart"],
		"Scatter Chart": gcr["Scatter Chart"]
      };
    }
    if (d3r) {
      $.pivotUtilities.locales.ru.d3_renderers = {
        "Treemap": d3r["Treemap"]
      };
    }
    if (c3r) {
      $.pivotUtilities.locales.ru.c3_renderers = {
        "C3 Line Chart": c3r["Line Chart"],
        "C3 Bar Chart": c3r["Bar Chart"],
        "C3 Stacked Bar Chart": c3r["Stacked Bar Chart"],
        "C3 Area Chart": c3r["Area Chart"],
		"C3 Horizontal Bar Chart": c3r["Horizontal Bar Chart"],
		"C3 Horizontal Stacked Bar Chart": c3r["Horizontal Stacked Bar Chart"],
		"C3 Scatter Chart": c3r["Scatter Chart"]
      };
    }
	if (plr) {
      $.pivotUtilities.locales.ru.plotly_renderers = {
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
		$.pivotUtilities.locales.ru.export_renderers = {
			"TSV экспорт" : er["TSV Export"]
		};
	}
	if (sr) {
		$.pivotUtilities.locales.ru.subtotal_renderers = {
			"Таблица с подитогом" : sr["Table With Subtotal"],
			"Таблица с подитогом Bar Chart" : sr["Table With Subtotal Bar Chart"],
			"Таблица с подитогом Heatmap" : sr["Table With Subtotal Heatmap"],
			"Таблица с подитогом Row Heatmap" : sr["Table With Subtotal Row Heatmap"],
			"Таблица с подитогом Col Heatmap" : sr["Table With Subtotal Col Heatmap"]
		};
	}
	
	return $.pivotUtilities.locales.ru;
  });

}).call(this);

//# sourceMappingURL=pivot.ru.js.map
