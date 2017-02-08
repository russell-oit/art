/* 
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

//https://github.com/davidguttman/react-pivot
//https://github.com/davidguttman/react-pivot/issues/49
//https://github.com/davidguttman/react-pivot/issues/28
//https://github.com/davidguttman/react-pivot-mean-slides/blob/master/client/no-frills-rg.js#L57
//https://github.com/davidguttman/react-pivot/blob/master/index.jsx
//https://github.com/davidguttman/react-pivot/blob/master/example/demo.jsx
//https://github.com/davidguttman/react-pivot/issues/50#issuecomment-277068638
//https://github.com/davidguttman/react-pivot/issues/5
//https://github.com/davidguttman/react-pivot/blob/master/example/basic.jsx
//https://github.com/davidguttman/react-pivot-classic-example
var rows = [
	{
		"firstName": "Francisco",
		"lastName": "Brekke",
		"state": "NY",
		"transaction": {
			"amount": "399.73",
			"date": "2012-02-02T08:00:00.000Z",
			"business": "Kozey-Moore",
			"name": "Checking Account 2297",
			"type": "deposit",
			"account": "82741327"
		}
	},
	{
		"firstName": "Francisco",
		"lastName": "Brekke",
		"state": "NY",
		"transaction": {
			"amount": "768.84",
			"date": "2012-02-02T08:00:00.000Z",
			"business": "Herman-Langworth",
			"name": "Money Market Account 9344",
			"type": "deposit",
			"account": "95753704"
		}
	}
];

var dimensions = [
	{value: 'firstName', title: 'First Name'},
	{value: 'lastName', title: 'Last Name'},
	{value: 'state', title: 'State'}
];

var reduce = function (row, memo) {
	// the memo object starts as {} for each group, build it up
	memo.count = (memo.count || 0) + 1;
	memo.amountTotal = (memo.amountTotal || 0) + parseFloat(row.transaction.amount);
	// be sure to return it when you're done for the next pass
	return memo;
};

var calculations = [
	{
		title: 'Count',
		value: 'count',
		className: 'alignRight'
	},
	{
		title: 'Amount',
		value: 'amountTotal',
		template: function (val, row) {
			return '$' + val.toFixed(2);
		},
		className: 'alignRight'
	},
	{
		title: 'Avg Amount',
		value: function (row) {
			return row.amountTotal / row.count;
		},
		template: function (val, row) {
			return '$' + val.toFixed(2);
		},
		className: 'alignRight'
	}
];

$.extend(options,{
	rows: rows,
	dimensions: dimensions,
	reduce: reduce,
	calculations: calculations
});
