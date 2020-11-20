import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {HttpParams} from "@angular/common/http";
import { throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable()
export class SearchService {
    protocol = "http";
    //server = "private-4b8135-rahulsinghkatoch.apiary-mock.com"
    server = "128.91.166.29:8088"
  constructor(private httpClient : HttpClient) { 
    
  }
  
  public get_products = function(params: HttpParams){
    return this.httpClient.get(this.protocol+"://"+this.server + '/search',{params}).pipe(
        map(data => data),
        catchError(this.handleError)
    );
  }

  handleError(error) {
    console.log(error);
    let errorMessage = '';
    if (error.error instanceof ErrorEvent) {
      // client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    window.alert(errorMessage);
    return throwError(errorMessage);
  }

}
