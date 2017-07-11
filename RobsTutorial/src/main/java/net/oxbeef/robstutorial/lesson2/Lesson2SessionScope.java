package net.oxbeef.robstutorial.lesson2;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class Lesson2SessionScope implements Serializable{
	
    private String name;
    private int count = 0;
    public Lesson2SessionScope() {
    }

    public String getName() {
        return name;
    }

    public void setName(String user_name) {
        this.name = user_name;
    }
    public int getCount() {
    	count = count +1;
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}