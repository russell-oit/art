var dimensions = [
	{value: 'City', title: 'City Name'},
	{value: 'Item', title: 'Item'}
];

var reduce = function (row, memo) {
	// the memo object starts as {} for each group, build it up
	memo.count = (memo.count || 0) + 1;
	memo.volumeTotal = (memo.volumeTotal || 0) + (parseFloat(row['Volume']) || 0);
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
		title: 'Volume',
		value: 'volumeTotal',
		className: 'alignRight'
	}
];

//use title attribute of the dimension
var activeDimensions = [
	'Item'
];

$.extend(options,{
	dimensions: dimensions,
	reduce: reduce,
	calculations: calculations,
	activeDimensions: activeDimensions
});
