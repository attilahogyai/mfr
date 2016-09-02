-- last uploads and users
select c.name,c.owner,(select count(1) from mfr.photo_category where category=c.id) as fotos,uc.last_login from 
mfr.category c, mfr.useracc uc where 
c.useracc=uc.id and uc.id >= 3710 
