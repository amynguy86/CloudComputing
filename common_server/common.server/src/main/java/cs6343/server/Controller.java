package cs6343.server;

import cs6343.data.Directory;
import cs6343.iface.MetadataServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller{

    MetadataServer mds;

    @RequestMapping("/ls")
    public String listPath(@RequestParam("path")String path){
        System.out.println(path); 
        return path;
    }

    @RequestMapping("/rm")
    public String removePath(@RequestParam("path")String path){
        System.out.println(path);
        return path;
    }

    @RequestMapping("/chmod")
    public String changeMode(@RequestParam("path")String path, @RequestParam("mode") String mode){
        System.out.println(path);
        System.out.println(mode);
        return path;
    }

    @RequestMapping("/touch")
    public String createFile(@RequestParam("path")String path){
        System.out.println(path);
        return path;
    }

    @RequestMapping("mkdir")
    public String makeDir(@RequestParam("path")String path){
        System.out.println(path);
        return path;
    }

}
