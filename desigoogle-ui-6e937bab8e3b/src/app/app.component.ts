import { Component, Injectable } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import {HttpParams} from "@angular/common/http";
import { SearchService } from './search.service';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/map';

interface SearchResult {
  url : string,
  docContent: string,
  title: string
}


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [ SearchService ]
})
export class AppComponent {
  searchControl: FormControl;
  dataservice: SearchService;

  constructor(private service: SearchService) {
    this.dataservice = service;
  }

  currentIndex = 0;

  paginationLimit = 10;

  results = []

  hasResult = false;

  errorHappened = false;

  queryFormGroup = new FormGroup({
    queryString: new FormControl()
  })

  getSearchResults = function() {

    this.errorHappened = false;

    let inputForm = this.queryFormGroup.get("queryString");

    if(inputForm === undefined || inputForm=== null) {
      return; 
    }

    if(inputForm.value === null || inputForm.value === null || inputForm.value.trim()==="") {
      return;
    }

    let queryString = "\""+inputForm.value+"\"";

    const params = new HttpParams().set('query',queryString)

    this.dataservice.get_products(params)
    .subscribe((response: SearchResult[]) => {
      console.log(response);
      this.hasResult = false;
      this.dbresults = response;
      if(this.dbresults.length!=0) {
        this.currentIndex = 0;
        this.getFirstPage();
      }
    },
    error => {
      this.errorHappened = true;
    })
 
  }

  getFirstPage = function() {
    if(this.currentIndex==0) {
      this.getNextPages();
    }
  }

  getNextPages = function() {
    if(this.currentIndex<this.dbresults.length) {
      let nextIndex = this.currentIndex + this.paginationLimit;
      if(nextIndex > this.dbresults.length) {
        nextIndex = this.currentIndex + this.dbresults.length%this.paginationLimit;
      }
      this.results = []
      while(this.currentIndex < nextIndex) {
        this.results.push(this.dbresults[this.currentIndex]);
        this.currentIndex++;
      }
      this.hasResult = true;
    }
  }

  getPrevPages = function() {
    if(this.currentIndex>=this.paginationLimit) {
      let offset = this.dbresults.length%this.paginationLimit;
      let tempIndex = this.currentIndex;
      if(offset>0 && tempIndex%this.paginationLimit!=0) {
        tempIndex = tempIndex - offset;
      } else if(offset==0 || tempIndex%this.paginationLimit==0) {
        tempIndex = tempIndex - this.paginationLimit;
      }
      if(tempIndex < this.paginationLimit) {
        tempIndex = this.paginationLimit;
      }
      let nextIndex = tempIndex - this.paginationLimit;
      if(nextIndex < 0) {
        nextIndex = 0;
      }
      this.results = []
      this.currentIndex = tempIndex;
      while(nextIndex < tempIndex) {
        this.results.push(this.dbresults[nextIndex]);
        nextIndex++;
      }
      this.hasResult = true;
    }
  }

  dbresults = []

  title = 'webapp';
}
