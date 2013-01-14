select au.username,au.update_date
from art_users au left outer join
(select username,max(update_time) update_time from art_logs
where type='login'
group by username) al
on au.USERNAME=al.USERNAME
where al.UPDATE_TIME is null and au.active_status='A'