@ECHO off

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET res_name=info.kgeorgiy.java.advanced.student
SET res_path=%wd%\modules\%res_name%\info\kgeorgiy\java\advanced\student

@ECHO on

run student AdvancedStudentGroupQuery StudentDB %res_path%\Student.java %res_path%\Group.java %res_path%\StudentQuery.java %res_path%\StudentGroupQuery.java %res_path%\AdvancedStudentGroupQuery.java %1