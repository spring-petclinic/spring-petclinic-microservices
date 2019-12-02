package org.springframework.samples.petclinic.vets.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/drill")
@RestController
public class Drill {

    public void newMethod1(){
        System.out.println("hello new method 1");
    }

    public void newMethod2(){
        System.out.println("hello new method 2");
    }

}
