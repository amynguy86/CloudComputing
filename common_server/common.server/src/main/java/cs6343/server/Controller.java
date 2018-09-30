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
    public String getPath(@RequestParam("path")String path){
        System.out.println(path); 
        return path;
    }

}
