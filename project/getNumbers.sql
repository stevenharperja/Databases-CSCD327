DELIMITER $$
create procedure getNumbers(in input_dept nvarchar(20), out num_instructors int, out num_students int)
BEGIN
select count(distinct instructor.ID)
into num_instructors
from instructor
where dept_name = input_dept;
select count(distinct student.ID)
into num_students
from student
where dept_name = input_dept;
END$$