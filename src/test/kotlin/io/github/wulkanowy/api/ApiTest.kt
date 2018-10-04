package io.github.wulkanowy.api

import io.github.wulkanowy.api.attendance.Attendance
import io.github.wulkanowy.api.attendance.AttendanceSummary
import io.github.wulkanowy.api.exams.Exam
import io.github.wulkanowy.api.grades.Grade
import io.github.wulkanowy.api.grades.GradeStatistics
import io.github.wulkanowy.api.grades.GradeSummary
import io.github.wulkanowy.api.homework.Homework
import io.github.wulkanowy.api.messages.Message
import io.github.wulkanowy.api.messages.Recipient
import io.github.wulkanowy.api.messages.ReportingUnit
import io.github.wulkanowy.api.mobile.Device
import io.github.wulkanowy.api.mobile.TokenResponse
import io.github.wulkanowy.api.notes.Note
import io.github.wulkanowy.api.realized.Realized
import io.github.wulkanowy.api.register.Pupil
import io.github.wulkanowy.api.register.Semester
import io.github.wulkanowy.api.register.StudentAndParentResponse
import io.github.wulkanowy.api.school.Teacher
import io.github.wulkanowy.api.student.StudentInfo
import io.github.wulkanowy.api.timetable.Timetable
import io.reactivex.observers.TestObserver
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore
class ApiTest : BaseTest() {

    private var api = Api()

    @Before
    fun setUp() {
        api.apply {
            logLevel = HttpLoggingInterceptor.Level.BASIC
            ssl = true
            host = "fakelog.cf"
            symbol = "Default"
            email = "jan@fakelog.cf"
            password = "jan123"
            schoolId = "123456"
            studentId = "1"
            diaryId = "101"
            notifyDataChanged() // unnecessary in this case
            setInterceptor(Interceptor { // important! put bellow notifyDataChanged
                println("Request event ${it.request().url().host()}")
                it.proceed(it.request())
            }, 0)
        }
    }

    @Test
    fun schoolInfoTest() {
        val info = api.getSchoolInfo()
        val infoObserver = TestObserver<StudentAndParentResponse>()
        info.subscribe(infoObserver)
        infoObserver.assertComplete()

        val values = infoObserver.values()[0]

        assertEquals("Publiczny dziennik Wulkanowego nr 1 w fakelog.cf", values.schoolName)
        assertEquals("III 2017", values.diaries[0].name)
        assertEquals("Jan Kowalski", values.students[0].name)
    }

    @Test
    fun pupilsTest() {
        val pupils = api.getPupils()
        val pupilsObserver = TestObserver<List<Pupil>>()
        pupils.subscribe(pupilsObserver)
        pupilsObserver.assertComplete()

        val values = pupilsObserver.values()[0]

        assertEquals("Default", values[0].symbol)
        assertEquals("jan@fakelog.cf", values[0].email)
        assertEquals("Jan Kowalski", values[0].studentName)
        assertEquals("123456", values[0].schoolId)
        assertEquals("1", values[0].studentId)
        assertEquals("Publiczny dziennik Wulkanowego nr 1 w fakelog.cf", values[0].schoolName)
    }

    @Test
    fun semestersTest() {
        val semesters = api.getSemesters()
        val semestersObserver = TestObserver<List<Semester>>()
        semesters.subscribe(semestersObserver)
        semestersObserver.assertComplete()

        val values = semestersObserver.values()[0]

        assertEquals("101", values[1].diaryId)
        assertEquals("1A 2015", values[1].diaryName)
        assertEquals(true, values[1].current)

        assertEquals("202", values[2].diaryId)
        assertEquals("II 2016", values[2].diaryName)

        assertEquals("303", values[4].diaryId)
        assertEquals("III 2017", values[4].diaryName)
        assertEquals(1234567, values[4].semesterId)
        assertEquals(1, values[4].semesterNumber)

        assertEquals(1234568, values[5].semesterId)
        assertEquals(2, values[5].semesterNumber)
    }

    @Test
    fun attendanceTest() {
        val attendance = api.getAttendance(getLocalDate(2018, 10, 1))
        val attendanceObserver = TestObserver<List<Attendance>>()
        attendance.subscribe(attendanceObserver)
        attendanceObserver.assertComplete()

        val values = attendanceObserver.values()[0]

        assertEquals(1, values[0].number)
        assertEquals("Zajęcia artystyczne", values[0].subject)
        assertEquals(getDate(2018, 10, 1), values[0].date)

        assertEquals("Obecność", values[0].name)
        assertTrue(values[0].presence)

        assertEquals("Nieobecność", values[1].name)
        assertTrue(values[1].absence)
        assertFalse(values[1].excused)

        assertEquals("Spóźnienie", values[3].name)
        assertEquals("Spóźnienie usprawiedliwione", values[4].name)
        assertEquals("Nieobecny z przyczyn szkolnych", values[5].name)
        assertEquals("Zwolniony", values[6].name)
        assertEquals("Obecność", values[9].name)

        assertEquals(1, values[0].number)
    }

    @Test
    fun attendanceSummaryTest() {
        val attendance = api.getAttendanceSummary()
        val attendanceObserver = TestObserver<List<AttendanceSummary>>()
        attendance.subscribe(attendanceObserver)
        attendanceObserver.assertComplete()

        val values = attendanceObserver.values()[0]

        assertEquals(12, values.size)

        assertEquals("IX", values[0].month)
        assertEquals(32, values[0].presence)
        assertEquals(1, values[0].absence)
        assertEquals(2, values[0].absenceExcused)
        assertEquals(3, values[0].absenceForSchoolReasons)
        assertEquals(4, values[0].lateness)
        assertEquals(5, values[0].latenessExcused)
        assertEquals(6, values[0].exemption)
        assertEquals(64, values[1].presence)
    }

    @Test
    fun examsTest() {
        val exams = api.getExams(getLocalDate(2018, 5, 7))
        val examsObserver = TestObserver<List<Exam>>()
        exams.subscribe(examsObserver)
        examsObserver.assertComplete()

        val values = examsObserver.values()[0]

        assertEquals(getDate(2018, 5, 9), values[0].date)
        assertEquals(getDate(2018, 4, 1), values[0].entryDate)
        assertEquals("Język angielski", values[0].subject)
        assertEquals("J1", values[0].group)
        assertEquals("Sprawdzian", values[0].type)
        assertEquals("słownictwo(kultura)", values[0].description)
        assertEquals("Anyż Zofia", values[0].teacher)
        assertEquals("AZ", values[0].teacherSymbol)
    }

    @Test
    fun homeworkTest() {
        val homework = api.getHomework(getLocalDate(2017, 10, 23))
        val homeworkObserver = TestObserver<List<Homework>>()
        homework.subscribe(homeworkObserver)
        homeworkObserver.assertComplete()

        val values = homeworkObserver.values()[0]

        assertEquals(getDate(2017, 10, 23), values[1].date)
        assertEquals(getDate(2017, 10, 18), values[1].entryDate)
        assertEquals("Metodologia programowania", values[1].subject)
        assertEquals("Wszystkie instrukcje warunkowe, pętle (budowa, zasada działania, schemat blokowy)", values[1].content)
        assertEquals("Janusz Tracz", values[1].teacher)
        assertEquals("TJ", values[1].teacherSymbol)
    }

    @Test
    fun notesTest() {
        val notes = api.getNotes()
        val notesObserver = TestObserver<List<Note>>()
        notes.subscribe(notesObserver)
        notesObserver.assertComplete()

        val values = notesObserver.values()[0]

        assertEquals(getDate(2018, 3, 26), values[0].date)
        assertEquals("Janusz Tracz", values[0].teacher)
        assertEquals("Udział w konkursie szkolnym +20 pkt", values[0].category)
        assertEquals("+ 20p za udział w Konkursie Języka Angielskiego", values[0].content)
    }

    @Test
    fun gradesTest() {
        val grades = api.getGrades(864)
        val gradesObserver = TestObserver<List<Grade>>()
        grades.subscribe(gradesObserver)
        gradesObserver.assertComplete()

        val values = gradesObserver.values()[0]

        assertEquals("Historia", values[0].subject)
        assertEquals("1", values[0].entry)
        assertEquals("000000", values[0].color)
        assertEquals("Spr", values[0].symbol)
        assertEquals("spr.-rozbiory", values[0].description)
        assertEquals("5,00", values[0].weight)
        assertEquals(5, values[0].weightValue)
        assertEquals(getDate(2018, 1, 29), values[0].date)
        assertEquals("Janusz Tracz", values[0].teacher)

        assertEquals("Bież", values[5].symbol)
        assertEquals("", values[5].description)
    }

    @Test
    fun gradesSummaryTest() {
        val summary = api.getGradesSummary(864)
        val summaryObserver = TestObserver<List<GradeSummary>>()
        summary.subscribe(summaryObserver)
        summaryObserver.assertComplete()

        val values = summaryObserver.values()[0]

        assertEquals("Etyka", values[2].name)
        assertEquals("2", values[2].predicted)
        assertEquals("2", values[2].final)

        assertEquals("Historia", values[5].name)
        assertEquals("1", values[5].predicted)
        assertEquals("1", values[5].final)

        assertEquals("Język niemiecki", values[8].name)
        assertEquals("", values[8].predicted)
        assertEquals("", values[8].final)
    }

    @Test
    fun gradesStatisticsTest() {
        val stats = api.getGradesStatistics(321, false)
        val statsObserver = TestObserver<List<GradeStatistics>>()
        stats.subscribe(statsObserver)

        val values = statsObserver.values()[0]

        assertEquals("Język polski", values[0].subject)
        assertEquals("Matematyka", values[7].subject)

        val annual = api.getGradesStatistics(123, true)
        val annualObserver = TestObserver<List<GradeStatistics>>()
        annual.subscribe(annualObserver)

        val values2 = annualObserver.values()[0]

        assertEquals("Język angielski", values2[0].subject)
    }

    @Test
    fun teachersTest() {
        val teachers = api.getTeachers()
        val teachersObserver = TestObserver<List<Teacher>>()
        teachers.subscribe(teachersObserver)
        teachersObserver.assertComplete()

        val values = teachersObserver.values()[0]

        assertEquals("Historia", values[1].subject)
        assertEquals("Janusz Tracz", values[1].name)
        assertEquals("TJ", values[1].short)
    }

    @Test
    fun studentInfoTest() {
        val student = api.getStudentInfo()
        val studentObserver = TestObserver<StudentInfo>()
        student.subscribe(studentObserver)
        studentObserver.assertComplete()

        val values = studentObserver.values()[0]

        assertEquals("Jan Marek Kowalski", values.student.fullName)
        assertEquals("Jan", values.student.firstName)
        assertEquals("Marek", values.student.secondName)
        assertEquals("Kowalski", values.student.surname)
        assertEquals(getDate(1970, 1, 1), values.student.birthDate)
        assertEquals("Warszawa", values.student.birthPlace)
        assertEquals("12345678900", values.student.pesel)
        assertEquals("Mężczyzna", values.student.gender)
        assertEquals("1", values.student.polishCitizenship)
        assertEquals("Nowak", values.student.familyName)
        assertEquals("Monika, Kamil", values.student.parentsNames)

        assertEquals("", values.student.address)
        assertEquals("", values.student.registeredAddress)
        assertEquals("", values.student.correspondenceAddress)

        assertEquals("", values.student.phoneNumber)
        assertEquals("-", values.student.cellPhoneNumber)
        assertEquals("jan@fakelog.cf", values.student.email)

        assertEquals("Monika Nowak", values.family[0].fullName)
        assertEquals("-", values.family[0].email)
        assertEquals("-", values.family[1].email)
    }

    @Test
    fun messagesTest() {
        val units = api.getReportingUnits()
        val unitsObserver = TestObserver<List<ReportingUnit>>()
        units.subscribe(unitsObserver)
        unitsObserver.assertComplete()

        val recipients = api.getRecipients()
        val recipientsObserver = TestObserver<List<Recipient>>()
        recipients.subscribe(recipientsObserver)
        recipientsObserver.assertComplete()

        val inbox = api.getReceivedMessages(getLocalDate(2015, 10, 5))
        val inboxObserver = TestObserver<List<Message>>()
        inbox.subscribe(inboxObserver)
        inboxObserver.assertComplete()

        assertEquals(2, inboxObserver.values()[0].size)

        val sent = api.getSentMessages()
        val outObserver = TestObserver<List<Message>>()
        sent.subscribe(outObserver)
        outObserver.assertComplete()

        assertEquals(1, outObserver.values()[0].size)

        val trash = api.getDeletedMessages()
        val trashObserver = TestObserver<List<Message>>()
        trash.subscribe(trashObserver)
        trashObserver.assertComplete()

        val del = trashObserver.values()[0]

        assertEquals(1, del.size)

        val m = api.getMessage(del[0].messageId, del[0].folderId)
        val mObserver = TestObserver<Message>()
        m.subscribe(mObserver)
        mObserver.assertComplete()

        assertEquals(1, mObserver.values().size)
    }

    @Test
    fun devicesTest() {
        val devices = api.getRegisteredDevices()
        val devicesObserver = TestObserver<List<Device>>()
        devices.subscribe(devicesObserver)
        devicesObserver.assertComplete()

        val values = devicesObserver.values()[0]

        assertEquals(2, values.size)
    }

    @Test
    fun tokenTest() {
        val token = api.getToken()
        val tokenObserver = TestObserver<TokenResponse>()
        token.subscribe(tokenObserver)
        tokenObserver.assertComplete()

        assertEquals("FK100000", tokenObserver.values()[0].token)
        assertEquals("Default", tokenObserver.values()[0].symbol)
        assertEquals("999999", tokenObserver.values()[0].pin)
    }

    @Test
    fun unregisterTest() {
        val unregister = api.unregisterDevice(1234)
        val unregisterObserver = TestObserver<List<Device>>()
        unregister.subscribe(unregisterObserver)
        unregisterObserver.assertComplete()

        assertEquals(2, unregisterObserver.values()[0].size)
    }

    @Test
    fun timetableTest() {
        val timetable = api.getTimetable(getLocalDate(2018, 9, 17))
        val timetableObserver = TestObserver<List<Timetable>>()
        timetable.subscribe(timetableObserver)
        timetableObserver.assertComplete()

        val values = timetableObserver.values()[0]

        assertEquals(0, values[0].number)
        assertEquals("Fizyka", values[0].subject)
        assertEquals("zaw2", values[0].group)
        assertEquals("uczniowie zwolnieni do domu", values[0].info)
        assertEquals(true, values[0].canceled)
        assertEquals(false, values[0].changes)
    }

    @Test
    fun realizedTest() {
        val realized = api.getRealized(getLocalDate(2018, 9, 17))
        val realizedObserver = TestObserver<List<Realized>>()
        realized.subscribe(realizedObserver)
        realizedObserver.assertComplete()

        val values = realizedObserver.values()[0]

        assertEquals(getDate(2018, 9, 17), values[0].date)
        assertEquals(1, values[0].number)
        assertEquals("Historia i społeczeństwo", values[0].subject)
        assertEquals("Powstanie listopadowe", values[0].topic)
        assertEquals("Histeryczna Jadwiga", values[0].teacher)
        assertEquals("Hi", values[0].teacherSymbol)
        assertEquals("Nieobecność nieusprawiedliwiona", values[0].absence)
    }
}
