/*****************************
Query the University Database
*****************************/
import java.io.*;
import java.sql.*;
import java.util.*;
import java.lang.String;

public class MyQuery {

    private Connection conn = null;
	 private Statement statement = null;
	 private ResultSet resultSet = null;

    
    public MyQuery(Connection c)throws SQLException
    {
        conn = c;
        // Statements allow to issue SQL queries to the database
        statement = conn.createStatement();
    }

    private void printSet() throws SQLException, IOException {
        //just a lazy quickprint method
        final int space_padding = 12;
        printSet(space_padding);
    }
    private void printSet(int padding) throws IOException, SQLException
    {
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int num_cols = rsMetaData.getColumnCount();

        final int space_padding = padding;

        //for each row
        while (resultSet.next()) {
            // It is possible to get the columns via name
            // also possible to get the columns via the column number which starts at 1
            String row = "";
            //for each col
            for(int i = 1; i< num_cols+1;i++) {
                String elem = resultSet.getString(i);
                if(elem == null){
                    elem = "";
                }
                //add some padding with space characters to make it look good
                for(int j = elem.length();j < space_padding; j++){
                    elem += " ";
                }
                row += elem;
            }

            System.out.println(row);
        }
    }
    public void findFall2009Students() throws SQLException
    {
        String query  = "select distinct name from student natural join takes where semester = \'Fall\' and year = 2009;";

        resultSet = statement.executeQuery(query);
    }
    
    public void printFall2009Students() throws IOException, SQLException
    {
	      System.out.println("******** Query 0 ********");
         System.out.println("name");
         while (resultSet.next()) {
			// It is possible to get the columns via name
			// also possible to get the columns via the column number which starts at 1
			String name = resultSet.getString(1);
         System.out.println(name);
   		}        
    }

    public void findGPAInfo() throws SQLException
    {
        String query = "select id, name, sum(numerical_grade * credits) / sum(credits) as GPA\n" +
                "from course join (\n" +
                "\tselect course_id,ID,\n" +
                "\t\tcase\n" +
                "\t\t\twhen grade = 'A' then 4.0\n" +
                "            when grade = 'A-' then 3.67\n" +
                "            when grade = 'B+' then 3.33\n" +
                "\t\t\twhen grade = 'B'then 3\n" +
                "\t\t\twhen grade = 'B-'then 2.67\n" +
                "\t\t\twhen grade = 'C+'then 2.33\n" +
                "\t\t\twhen grade = 'C'then 2\n" +
                "\t\t\twhen grade = 'C-'then 1.67\n" +
                "\t\t\twhen grade = 'D+'then 1.33\n" +
                "\t\t\twhen grade = 'D'then 1\n" +
                "\t\t\twhen grade = 'D-'then 0.67\n" +
                "\t\t\twhen grade = 'F'then 0\n" +
                "            else null\n" +
                "\t\tend\n" +
                "\t\tas numerical_grade\n" +
                "\tfrom takes\n" +
                "    where not grade is null\n" +
                "    ) as T using(course_id)\n" +
                "join student using(ID)\n" +
                "group by ID\n";
        resultSet = statement.executeQuery(query);
    }
    
    public void printGPAInfo() throws IOException, SQLException
    {
		   System.out.println("******** Query 1 ********");
           printSet();
    }

    public void findMorningCourses() throws SQLException
    {
        String query = "select course_id, sec_id, title, semester, year, name, count(distinct takes.ID) as enrollment\n" +
                "from takes natural join course natural join section natural join time_slot join teaches using(course_id,sec_id,semester,year) join instructor on teaches.ID = instructor.ID\n" +
                "where time_slot.start_hr <= 12 \n" +
                "group by course_id,sec_id,semester,year,instructor.name";
        resultSet = statement.executeQuery(query);
    }

    public void printMorningCourses() throws IOException, SQLException
    {
	   	System.out.println("******** Query 2 ********");
        printSet(27);
    }

    public void findBusyInstructor() throws SQLException
    {
        String query = "select name\n" +
                "from instructor Q1\n" +
                "where \n" +
                "(\n" +
                "\tselect count(course_id)\n" +
                "    from teaches\n" +
                "    where teaches.ID = Q1.ID\n" +
                ")  >= all\n" +
                "(\n" +
                "\tselect count(course_id)\n" +
                "    from teaches\n" +
                "    group by ID\n" +
                ")\n";
        resultSet = statement.executeQuery(query);
    }

    public void printBusyInstructor() throws IOException, SQLException
    {
		   System.out.println("******** Query 3 ********");
        printSet();
    }

    public void findPrereq() throws SQLException
    {
        String query = "select C1.title, C2.title as prereq\n" +
            "from course C1 left outer join prereq using(course_id) left outer join course C2 on prereq_id = C2.course_id\n";
        resultSet = statement.executeQuery(query);
    }

    public void printPrereq() throws IOException, SQLException
    {
		   System.out.println("******** Query 4 ********");
        printSet(27);
    }

    public void updateTable() throws SQLException
    {
        String update = "update studentCopy \n" +
                "set tot_cred = (\n" +
                "\n" +
                "select ifnull(sum(\n" +
                "\tcase\n" +
                "    when grade is null then 0\n" +
                "    when grade = 'F' then 0\n" +
                "\telse credits\n" +
                "    end), 0)\n" +
                "from course join takes using (course_id)\n" +
                "where studentCopy.ID = takes.ID\n" +
                ")\n";
        String query = "SELECT * FROM s22sharper12_universityDB.studentCopy;";
        resultSet = statement.executeQuery(query);
    }

    public void printUpdatedTable() throws IOException, SQLException
    {
        System.out.println("******** Query 5 ********");
        printSet();

    }
	
	 public void findHeadCounts() throws SQLException, IOException {
         String procedure = "DELIMITER $$\n" +
                 "create procedure getNumbers(in input_dept nvarchar(20), out num_instructors int, out num_students int)\n" +
                 "BEGIN\n" +
                 "select count(distinct instructor.ID)\n" +
                 "into num_instructors\n" +
                 "from instructor\n" +
                 "where dept_name = input_dept;\n" +
                 "select count(distinct student.ID)\n" +
                 "into num_students\n" +
                 "from student\n" +
                 "where dept_name = input_dept;\n" +
                 "END$$";
         String query = "call getNumbers(\'Comp. Sci.\', @inst, @stud);\n" +
                 "select @inst as instructor, @stud as student";

         CallableStatement cStmt = conn.prepareCall("{call getNumbers(?,?,?)}");

         //get user input
         Scanner myObj = new Scanner(System.in);
         System.out.println("Please enter the department name:");
         String department = myObj.nextLine();

         cStmt.setString(1,department);
         cStmt.registerOutParameter(2, Types.INTEGER);
         cStmt.registerOutParameter(3, Types.INTEGER);


         cStmt.execute();

         int num_instructors = cStmt.getInt(2);
         int num_students = cStmt.getInt(3);


		  System.out.println("******** Query 6 ********");
         System.out.println(num_instructors + ", " + num_students);
	 }
    
    
    public void findFirstLastSemester() throws SQLException
    {
        String query = "select distinct S1.ID, S1.name, concat(Min2.semester, ' ' , Min2.year) as First_Semester, concat(Max2.semester, ' ' , Max2.year) as Last_Semester\n" +
                "from student S1 join\n" +
                "(\n" +
                "\tselect case\n" +
                "\t\twhen semester = 'Spring' then 'Spring'\n" +
                "        when semester = 'Summer' and not exists\n" +
                "        (\n" +
                "\t\t\tselect *\n" +
                "            from takes\n" +
                "            where year = Min1.year and takes.ID = Min1.ID and semester = 'Spring'\n" +
                "        )\n" +
                "        then 'Summer'\n" +
                "        when semester = 'Fall' and not exists\n" +
                "        (\n" +
                "\t\t\tselect *\n" +
                "            from takes\n" +
                "            where year = Min1.year and takes.ID = Min1.ID and semester = 'Summer'\n" +
                "        ) and not exists\n" +
                "        (\n" +
                "\t\t\tselect *\n" +
                "            from takes\n" +
                "            where year = Min1.year and takes.ID = Min1.ID and semester = 'Spring'\n" +
                "        )\n" +
                "        then 'Fall'\n" +
                "        end as semester\n" +
                "        ,\n" +
                "        Min1.year as year\n" +
                "        ,\n" +
                "        takes.ID as ID\n" +
                "\tfrom\n" +
                "\t(\n" +
                "\t\tselect min(year) as year,ID\n" +
                "\t\tfrom takes\n" +
                "        group by ID\n" +
                "\t) Min1 join takes on(Min1.year = takes.year and Min1.ID = takes.ID)\n" +
                "    \n" +
                ") Min2\n" +
                "using (ID) join\n" +
                "(\n" +
                "\tselect case\n" +
                "\t\twhen semester = 'Fall' then 'Fall'\n" +
                "        when semester = 'Summer' and not exists\n" +
                "        (\n" +
                "\t\t\tselect *\n" +
                "            from takes\n" +
                "            where year = Max1.year and takes.ID = Max1.ID and semester = 'Fall'\n" +
                "        )\n" +
                "        then 'Summer'\n" +
                "        when semester = 'Spring' and not exists\n" +
                "        (\n" +
                "\t\t\tselect *\n" +
                "            from takes\n" +
                "            where year = Max1.year and takes.ID = Max1.ID and semester = 'Summer'\n" +
                "        ) and not exists\n" +
                "        (\n" +
                "\t\t\tselect *\n" +
                "            from takes\n" +
                "            where year = Max1.year and takes.ID = Max1.ID and semester = 'Fall'\n" +
                "        )\n" +
                "        then 'Spring'\n" +
                "        end as semester\n" +
                "        ,\n" +
                "        Max1.year as year\n" +
                "        ,\n" +
                "        takes.ID as ID\n" +
                "\tfrom\n" +
                "\t(\n" +
                "\t\tselect max(year) as year,ID\n" +
                "\t\tfrom takes\n" +
                "\t\tgroup by ID\n" +
                "\t) Max1 join takes on(Max1.year = takes.year and Max1.ID = takes.ID)\n" +
                "    \n" +
                ")Max2\n" +
                "using(ID)\n" +
                "where Min2.semester is not null and Min2.year is not null and Max2.semester is not null and Max2.semester is not null";
        resultSet = statement.executeQuery(query);
    }

    public void printFirstLastSemester() throws IOException, SQLException
    {
        System.out.println("******** Query 7 ********");
        printSet();
    }

}
