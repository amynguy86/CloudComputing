package cs6343;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataServer {

    private static final int MILLISECONDS = 200;
    RealDistribution distribution = new ExponentialDistribution(5.0);

    @RequestMapping(path="/read", method = RequestMethod.GET)
    public String readFile(@RequestParam("filename") String filename){
        try {
            double sample = distribution.sample();
            System.out.println("Filename: " + filename + " sleep: " + MILLISECONDS * sample);
            Thread.sleep((int) (MILLISECONDS  * sample));
        } catch (InterruptedException e) {
        }
        return "Done";
    }

    @RequestMapping(path="/write", method = RequestMethod.GET)
    public String writeFile(@RequestParam("filename") String filename){
        try {
            double sample = distribution.sample();
            System.out.println("Filename: " + filename + " sleep: " + MILLISECONDS * sample);
            Thread.sleep((int)(MILLISECONDS *  sample));
        } catch (InterruptedException e) {
        }
        return "Done";
    }

}
