select distinct S1.ID, S1.name, concat(Min2.semester, ' ' , Min2.year) as First_Semester, concat(Max2.semester, ' ' , Max2.year) as Last_Semester
from student S1 join
(
	select case
		when semester = 'Spring' then 'Spring'
        when semester = 'Summer' and not exists
        (
			select *
            from takes
            where year = Min1.year and takes.ID = Min1.ID and semester = 'Spring'
        )
        then 'Summer'
        when semester = 'Fall' and not exists
        (
			select *
            from takes
            where year = Min1.year and takes.ID = Min1.ID and semester = 'Summer'
        ) and not exists
        (
			select *
            from takes
            where year = Min1.year and takes.ID = Min1.ID and semester = 'Spring'
        )
        then 'Fall'
        end as semester
        ,
        Min1.year as year
        ,
        takes.ID as ID
	from
	(
		select min(year) as year,ID
		from takes
        group by ID
	) Min1 join takes on(Min1.year = takes.year and Min1.ID = takes.ID)
    
) Min2
using (ID) join
(
	select case
		when semester = 'Fall' then 'Fall'
        when semester = 'Summer' and not exists
        (
			select *
            from takes
            where year = Max1.year and takes.ID = Max1.ID and semester = 'Fall'
        )
        then 'Summer'
        when semester = 'Spring' and not exists
        (
			select *
            from takes
            where year = Max1.year and takes.ID = Max1.ID and semester = 'Summer'
        ) and not exists
        (
			select *
            from takes
            where year = Max1.year and takes.ID = Max1.ID and semester = 'Fall'
        )
        then 'Spring'
        end as semester
        ,
        Max1.year as year
        ,
        takes.ID as ID
	from
	(
		select max(year) as year,ID
		from takes
		group by ID
	) Max1 join takes on(Max1.year = takes.year and Max1.ID = takes.ID)
    
)Max2
using(ID)
where Min2.semester is not null and Min2.year is not null and Max2.semester is not null and Max2.semester is not null