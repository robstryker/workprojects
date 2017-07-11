package net.oxbeef.robstutorial.lesson2;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class Lesson2RequestScope{
	
    private String name;
    private int count = 0;
    public Lesson2RequestScope() {
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