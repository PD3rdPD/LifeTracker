package classes;

import java.util.ArrayList;

public class ClassManager {

    private ArrayList<Class> classes;

    public ClassManager() {
        classes = new ArrayList<>();
    }

    public void addClass(Class classData) {
        classes.add(classData);
    }

    public ArrayList<Class> getClasses() {
        return classes;
    }

    public Class findClass(String className) {
        for (Class classData : classes) {
            if (classData.getClassName().equals(className)) {
                return classData;
            }
        }

        return null;
    }
}