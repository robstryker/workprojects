package net.oxbeef.robstutorial.lesson2;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class Lesson2ApplicationScope{
	
    private String name;
    private int count = 0;


	public Lesson2ApplicationScope() {
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