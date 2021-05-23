package com.arch.training.demo.controller;

import com.arch.training.demo.server.Node;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2021/5/18
 */
@RestController
public class Home {
	@RequestMapping(value = {"/", "/index", "/home"}, method = RequestMethod.GET)
	public String home() throws Exception {

		return Node.INSTANCE.getRole();
	}
}
