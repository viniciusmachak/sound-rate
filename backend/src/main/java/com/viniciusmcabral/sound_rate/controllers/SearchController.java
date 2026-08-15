package com.viniciusmcabral.sound_rate.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.viniciusmcabral.sound_rate.dtos.response.SearchResultDTO;
import com.viniciusmcabral.sound_rate.services.SearchService;

import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/v1/search")
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping
	public List<SearchResultDTO> search(@RequestParam("query") @NotBlank String query) {
		return searchService.searchAll(query);
	}
}
