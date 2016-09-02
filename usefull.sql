-- user, category, photo_count ordered by last login
select ua.login,ua.last_login, c.name,(select count(1) from mfr.photo_category where category=c.id)  from mfr.useracc ua left join mfr.category c on ua.id=c.useracc
group by ua.login,ua.last_login,ua.id,c.id,c.name  order by ua.last_login
