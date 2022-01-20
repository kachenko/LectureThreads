package myStudentThreads;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Lecture {
	private boolean isAsked = false;
	private boolean isAnswered = false;
	int countQuestion = 0;
	
	Lock locker;
	Condition condL;
	Condition condS;
	
	Lecture() {
		locker = new ReentrantLock();
		condL = locker.newCondition();
		condS = locker.newCondition();
	}
	
	public boolean isAsked() {
		return isAsked;
	}

	public void setAsked(boolean isAsked) {
		this.isAsked = isAsked;
	}

	public boolean isAnswered() {
		return isAnswered;
	}

	public void setAnswered(boolean isAnswered) {
		this.isAnswered = isAnswered;
	}
}


class Lecturer implements Runnable {
	Lecture l;
	Lecturer(Lecture l) {
		this.l = l;
		//System.out.println("The lecturer is created.");
	}
	@Override
	public void run() {
		for(int i=0; i<20; i++) {
			l.locker.lock();
			try {
				while(l.isAsked() && !l.isAnswered())
					l.condL.await();
				l.setAsked(true);
				l.setAnswered(false);
				l.countQuestion++;
				System.out.println("The lecturer asked a " + l.countQuestion + " question.");
				l.condS.signal();
			} catch (InterruptedException e) {
				System.out.println("LECTURER THREAD ERROR: " + e.getMessage());
			} finally {
				l.locker.unlock();
			}
		}
		l.countQuestion = 0;
	}
}


class Student implements Runnable {
	static Random rand = new Random();
	Lecture l;
	private int studentID;
	
	

	static Map<Student, Boolean> studentsAnswers = new HashMap<Student, Boolean>();
	static Map<Student, Integer> studentsPositive = new HashMap<Student, Integer>();
	
	Student(Lecture l, int studentID) {
		this.l = l;
		this.studentID = studentID;
		//System.out.println("The student "+studentID+" is created.");
	}
	
	public int getStudentID() {
		return studentID;
	}
	
	public String toString() {
		return "Student " + studentID;
	}
	
	public static void addStudentsAnswers(Student student) {
		boolean answer = rand.nextBoolean();
		studentsAnswers.put(student, answer);
	}
	
	public static int getPositiveAnswers(Student student) {
		return studentsPositive.get(student);
	}
	
	public static Map<Student, Integer> sortedMap() {
		Map<Student, Integer> sorted = new LinkedHashMap<>();
		studentsPositive.entrySet()
		    .stream()
		    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) 
		    .forEachOrdered(x -> sorted.put(x.getKey(), x.getValue()));
		return sorted;
	}
	
	
	@Override
	public void run() {
			l.locker.lock();
			try {
				while((!l.isAsked() && l.isAnswered()) ||
						!l.isAsked() && !l.isAnswered())
					l.condS.await();
				
				if(l.countQuestion == 21) {
					l.condS.signalAll();
					return; 
				}
				l.setAsked(false);
				l.setAnswered(true);
				System.out.println("The student " + studentID + " answered to a " + l.countQuestion + " question.");
				l.condL.signal();
				l.condS.signal();
			} catch (InterruptedException e) {
				System.out.println("STUDENT THREAD ERROR: " + e.getMessage());
			} finally {
				l.locker.unlock();
			}
	}
}


