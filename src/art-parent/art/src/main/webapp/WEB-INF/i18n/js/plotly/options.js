//https://xem.github.io/terser-online/
//https://github.com/plotly/plotly.js/blob/master/src/locale-en.js
//https://github.com/plotly/plotly.js/blob/master/tasks/util/constants.js
//https://github.com/plotly/plotly.js/tree/master/lib/locales
//https://github.com/plotly/plotly.js/issues/856
//https://github.com/terser-js/terser/issues/106
//https://github.com/plotly/plotly.js/blob/master/dist/translation-keys.txt
//https://github.com/d3/d3-time-format
//https://github.com/moment/moment/tree/develop/locale
var options =
{
	ecma: 5,
	mangle: true,
	compress: {           
		typeofs: false
	},
	output: {
		beautify: false,
		ascii_only: true
	},
	sourceMap: false
}
;