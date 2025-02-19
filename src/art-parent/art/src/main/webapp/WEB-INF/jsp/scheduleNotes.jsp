<%-- 
    Document   : scheduleNotes
    Created on : 30-Jan-2017, 12:53:27
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%-- Schedule field value documentation. Based on quartz javadoc - CronExpression class --%>
<%-- http://www.quartz-scheduler.org/api/2.2.1/org/quartz/CronExpression.html --%>
<table class="table">
	<tr>
		<th style="text-align:left;">Field</th>
		<th style="text-align:left;">&nbsp;</th>
		<th style="text-align:left;">Allowed Values</th>
		<th style="text-align:left;">&nbsp;</th>
		<th style="text-align:left;">Allowed Special Characters</th>
	</tr>
	<tr>
		<td style="text-align:left;"><code>Second</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>0-59</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>, - * /</code></td>
	</tr>
	<tr>
		<td style="text-align:left;"><code>Minute</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>0-59</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>, - * /</code></td>
	</tr>
	<tr>
		<td style="text-align:left;"><code>Hour</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>0-23</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>, - * /</code></td>
	</tr>
	<tr>
		<td style="text-align:left;"><code>Day (Day of the month)</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>1-31</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>, - * ? / L W</code></td>
	</tr>
	<tr>
		<td style="text-align:left;"><code>Month</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>1-12 or JAN-DEC</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>, - * /</code></td>
	</tr>
	<tr>
		<td style="text-align:left;"><code>Week Day (Day of the week)</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>1-7 or SUN-SAT</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>, - * ? / L #</code></td>
	</tr>
	<tr>
		<td style="text-align:left;"><code>Year</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>1970-2199</code></td>
		<td style="text-align:left;">&nbsp;</td>
		<td style="text-align:left;"><code>, - * /</code></td>
	</tr>
</table>

<ul>
	<li>
		The '*' character is used to specify all values. For example, &quot;*&quot; 
		in the minute field means &quot;every minute&quot;. </li>
	<li>
		The '-' character is used to specify ranges. For example, &quot;10-12&quot; in
		the hour field means &quot;the hours 10, 11 and 12&quot;. </li>
	<li>
		The ',' character is used to specify additional values. For example,
		&quot;MON,WED,FRI&quot; in the week day field means &quot;the days Monday,
		Wednesday, and Friday&quot;. </li>
	<li>
		The '/' character is used to specify increments. For example, &quot;0/15&quot;
		in the minute field means &quot;the minute 0, and every 15 minutes afterwards&quot;
		i.e. the minutes 0, 15, 30, and 45.
		Essentially, for each field in the expression, there is a set of numbers that can
		be turned on or off. For seconds and minutes, the numbers range from 0 to 59. 
		For hours 0 to 23, for days of the month 0 to 31, and for months 1 to 12. 
		The "/" character simply helps you turn on every "nth" value in the given set. 
		Thus "7/6" in the month field only turns on month "7", it does NOT mean every 6th month.
	</li> 
	<li>
		The '?' character is allowed for the day of the month and week day fields. It
		is used to specify "no specific value". It is not possible to define specific values for both
		the day of the month and the week day fields. If you specify a value in the day of the month field, set the week day field
		to &quot;?&quot;. If you specify a value in the week day field,
		set the day of the month to &quot;?&quot;.</li>
	<li>
		The 'L' character is allowed for the day of the month and week day fields.
		It is short-hand for &quot;last&quot;. For example, &quot;L&quot; in 
		the day of the month field means &quot;the last day of the month&quot; - day 31 
		for January, day 28 for February on non-leap years etc. The value &quot;L&quot; in 
		week day field is equivalent to &quot;7&quot; or &quot;SAT&quot; i.e Saturday. 
		If used in the week day field after another value, it
		means &quot;the last certain weekday of the month&quot; - for example &quot;6L&quot;
		means &quot;the last Friday of the month&quot;.
		You can also specify an offset from the last day of the month, e.g &quot;L-3&quot; in the 
		day of the month field would mean the third-to-last day of the calendar month.
		When using the 'L' option, 
		do not specify lists or ranges of values. </li>
	<li>
		The 'W' character is allowed for the day of the month field. It 
		is used to specify the weekday (Monday-Friday) nearest the given day.  For example,
		&quot;15W&quot; in the day of the month field means &quot;the nearest weekday to the 15th of
		the month&quot;. So if the 15th is a Saturday, the job will run on 
		Friday the 14th. If the 15th is a Sunday, the job will run on Monday the
		16th. If the 15th is a Tuesday, then it will run on Tuesday the 15th. 
		If &quot;1W&quot; is specified as the day of the month and the
		1st is a Saturday, the job will run on Monday the 3rd. It will not 
		cross the month boundary. When using the 'W' option, do not specify lists
		or ranges of days. </li>
	<li>
		The 'L' and 'W' characters can also be combined in the day of the month field to yield 'LW', 
		which translates to &quot;last weekday of the month&quot;. 
	</li>
	<li>
		The '#' character is allowed for the week day field. It is
		used to specify &quot;the nth certain weekday day of the month&quot;. For example, the 
		value &quot;6#3&quot; in the week day field means &quot;the third Friday of 
		the month&quot; (day 6 = Friday and &quot;#3&quot; = the 3rd one in the month). 
		Another example: &quot;2#1&quot; means &quot;the first Monday of the month&quot;. If the '#' character is used, there can
		only be one value in the field. No additional values are allowed e.g. &quot;3#1,6#3&quot; is 
		not allowed. </li> 
	<li>
		The legal characters and the names of months and days of the week are not
		case sensitive. </li>
	<li>
		Overflowing ranges is supported - that is, having a larger number on the left hand side
		than the right e.g. 22-2 in the hour field to catch 10 o'clock at night until 2 o'clock in the morning,
		or NOV-FEB in the month field.
	</li>
	<li>
		See <a href="http://www.quartz-scheduler.org/api/2.2.1/org/quartz/CronExpression.html">
			http://www.quartz-scheduler.org/api/2.2.1/org/quartz/CronExpression.html
		</a>
	</li>
</ul>

