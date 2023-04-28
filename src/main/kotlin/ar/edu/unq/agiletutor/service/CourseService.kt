package ar.edu.unq.agiletutor.service

import ar.edu.unq.agiletutor.ItemNotFoundException
import ar.edu.unq.agiletutor.UsernameExistException
import ar.edu.unq.agiletutor.model.Course
import ar.edu.unq.agiletutor.model.Student
import ar.edu.unq.agiletutor.model.Tutor
import ar.edu.unq.agiletutor.persistence.AttendanceRepository
import ar.edu.unq.agiletutor.persistence.CourseRepository
import ar.edu.unq.agiletutor.persistence.StudentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService {

    @Autowired
    private lateinit var tutorService: TutorService

    @Autowired
    private lateinit var repository: CourseRepository

    @Autowired
    private lateinit var studenRepository: StudentRepository

    @Autowired
    private lateinit var senderService: EmailServiceImpl

    @Autowired
    private lateinit var attendanceRepository: AttendanceRepository

    @Transactional
    fun register(course: Course): Course {

        if (existSCourse(course)) {
            throw UsernameExistException("Course with name:  ${course.name} is used")
        }

        return repository.save(course)
    }

    private fun existSCourse(course: Course): Boolean {
        var bool = false
        val courses = repository.findAll().toMutableList()
        if (courses.isNotEmpty()) {
            bool = courses.any { it.name == course.name }
        }
        return bool
    }

    @Transactional
    fun findAll(): List<Course> {
        return repository.findAll()
    }

    @Transactional
    fun findByID(id: Int): Course {
        val course = repository.findById(id)
        if (!(course.isPresent)) {
            throw ItemNotFoundException("Course with Id:  $id not found")
        }
        return course.get()
    }

    @Transactional
    fun tutorFromACourse(id: Int): Tutor {
        val course = findByID(id)
        return course.tutor!!
    }

    fun studentsFromACourse(id: Int): List<Student> {
        val course = findByID(id)
        return course.students.toMutableList()
    }

    fun studentsFromATutor(id: Int): List<Student> {
        val students = mutableListOf<Student>()
        val courses = tutorService.coursesFromATutor(id)
        for (course in courses) {
            students.addAll(studentsFromACourse(course.id!!))
        }
        return students
    }

    @Transactional
    fun updateStudentsAttendancesFromACourse(courseId: Int, studentAttendance: List<StudentAttendanceDTO>) {
        //val course = findByID(id)
        studentAttendance.forEach {
            attendanceRepository.setAttendanceInfoById(it.attendance.attended.toBoolean(), it.attendance.id)
        }
        senderService.notifyAllAbsent(studentAttendance[0].attendance.day!!, courseId)
       /*     var studentToUpdate = studenRepository.findById(it.studentId.toLong()).get()
            var atendancesUpdated = updateAttendance(
                it.attendance.day!!,
                it.attendance.attended.toBoolean(),
                studentToUpdate.attendances.toMutableList()
            )
            println(studentToUpdate.email + " dia:" + studentAttendance[0].attendance.day + " curso:" + studentToUpdate.course!!.id + "paraupdate")
            studentToUpdate.attendances = atendancesUpdated.toMutableSet()
            for (attendance in studentToUpdate.attendances) {
                println(attendance.attended.toString()+"booleano-dia"+attendance.day)
            }
            studenRepository.save(studentToUpdate)
        }
        senderService.notifyAllAbsent(studentAttendance[0].attendance.day!!, courseId)
        *//*      for (student in course.students) {
                  val boolean = booleans.iterator().next()
                  updateAttendance(day,boolean, student.attendances.toMutableList())
        repository.save(course)*/
    }
/*
    private fun updateAttendance(
        day: Int,
        boolean: Boolean,
        attendances: MutableList<Attendance>
    ): MutableList<Attendance> {
        println("$day $boolean aquiestoy")
        val attendance = attendances[day]
        attendance.attended = boolean
        attendances[day] = attendance
        return attendances
    }*/
}