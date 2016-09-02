import java.text.SimpleDateFormat;
import java.util.Date;

import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.utils.Span;


public class JcTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Span s=Chronic.parse("2012:02:04 13:09:51");
		Date d=new Date();
		d.setTime(s.getBegin());
		
		System.out.println(d);
		d.setTime(s.getEnd());
		System.out.println(d);
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		System.out.println(sdf.format(new Date()));
		

	}

}
