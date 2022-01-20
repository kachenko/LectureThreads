package myStudentThreads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String[] args) {
		
		Lecture lecture = new Lecture();
		Lecturer lecturer = new Lecturer(lecture);
		
		for(int i=0; i<20; i++)
			Student.addStudentsAnswers(new Student(lecture, i+1));
		
		for(Student student : Student.studentsAnswers.keySet())
			Student.studentsPositive.put(student, 0);
		
		for(int j=0; j<5; j++) {
			ExecutorService exe = Executors.newFixedThreadPool(21);
			
			List<Callable<Object>> list = new ArrayList<>();
			list.add(Executors.callable(lecturer));
			for(Student student : Student.studentsAnswers.keySet()) {
				if(Student.studentsAnswers.get(student) == null)
					Student.addStudentsAnswers(student);
				if(Student.studentsAnswers.get(student) == true) {
					int countPositive = Student.studentsPositive.get(student) + 1;
					Student.studentsPositive.put(student, countPositive);
				}
				list.add(Executors.callable(student));
			}
			try {
				exe.invokeAll(list);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			exe.shutdown();
			for(Map.Entry<Student, Boolean> entry : Student.studentsAnswers.entrySet())
				entry.setValue(null);
			System.out.println("END.\n");
		}
		
		System.out.println("=== RATING OF CORRECT ANSWERS ===");
		for(Map.Entry<Student, Integer> entry : Student.sortedMap().entrySet())
			System.out.println(entry.getKey() + " - " + entry.getValue());

	}
}
