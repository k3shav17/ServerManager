import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { NotifierService } from 'angular-notifier';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, startWith } from 'rxjs/operators';
import { DataState } from './enum/data-state.enum';
import { Status } from './enum/status.enum';
import { AppState } from './interface/app-state';
import { CustomResponse } from './interface/custom-response';
import { Server } from './interface/server';
import { NotificationService } from './service/notification.service';
import { ServerService } from './service/server.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent implements OnInit {
  appState$: Observable<AppState<CustomResponse>>;
  readonly DataState = DataState;
  readonly Status = Status;
  private filterSubject = new BehaviorSubject<string>('');
  private dataSubject = new BehaviorSubject<CustomResponse>(null);
  filterStatus$ = this.filterSubject.asObservable();
  private isLoading = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoading.asObservable();

  constructor(private serverService: ServerService, private notifier:NotificationService) {}

  ngOnInit() {
    this.appState$ = this.serverService.server$.pipe(
      map((response) => {
        console.log(response);
        this.notifier.onDefault(response.message);
        this.dataSubject.next(response);
        return { dataState: DataState.LOADED, appData: {...response, data: { servers: response.data.servers.reverse()}}};
      }),
      startWith({ dataState: DataState.LOADING }),
      catchError((error: string) => {

        this.notifier.onDefault(error);
        return of({ dataState: DataState.ERROR, error });
      })
    );
  }

  pingServer(ipAddress: string) {
    this.filterSubject.next(ipAddress);
    this.appState$ = this.serverService.ping$(ipAddress).pipe(
      map((response) => {

        const index = this.dataSubject.value.data.servers.findIndex(
          (server) => server.id === response.data.server.id
        );

        this.notifier.onDefault(response.message);
        this.dataSubject.value.data.servers[index] = response.data.server;
        this.filterSubject.next('');
        return { dataState: DataState.LOADED, appData: this.dataSubject.value };
      }),
      startWith({
        dataState: DataState.LOADED,
        appData: this.dataSubject.value,
      }),
      catchError((error: string) => {
        this.notifier.onDefault(error);
        this.filterSubject.next('');
        return of({ dataState: DataState.ERROR, error });
      })
    );
  }
  filterServers(status: Status) {
    this.appState$ = this.serverService
      .filter$(status, this.dataSubject.value)
      .pipe(
        map((response) => {
        this.notifier.onDefault(response.message);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({
          dataState: DataState.LOADED,
          appData: this.dataSubject.value,
        }),
        catchError((error: string) => {
        this.notifier.onDefault(error);
          return of({ dataState: DataState.ERROR, error });
        })
      );
  }

  saveServer(serverForm: NgForm) {
    this.isLoading.next(true);
    this.appState$ = this.serverService.save$(serverForm.value).pipe(
      map((response) => {
        this.dataSubject.next({
          ...response,
          data: {
            servers: [
              response.data.server,
              ...this.dataSubject.value.data.servers,
            ],
          },
        });
        this.notifier.onDefault(response.message);
        document.getElementById('closeModal').click();

        this.isLoading.next(false);
        serverForm.resetForm({ status: this.Status.SERVER_DOWN });
        return { dataState: DataState.LOADED, appData: this.dataSubject.value };
      }),
      startWith({
        dataState: DataState.LOADED,
        appData: this.dataSubject.value,
      }),
      catchError((error: string) => {
        this.notifier.onDefault(error);
        this.isLoading.next(false);
        return of({ dataState: DataState.ERROR, error });
      })
    );
  }
  deleteServer(server: Server) {
    this.appState$ = this.serverService.delete$(server.id).pipe(
      map((response) => {
        this.dataSubject.next({
          ...response,
          data: {
            servers: this.dataSubject.value.data.servers.filter(
              (s) => s.id !== server.id
            ),
          },
        });
        this.notifier.onDefault(response.message);
        return { dataState: DataState.LOADED, appData: this.dataSubject.value };
      }),
      startWith({
        dataState: DataState.LOADED,
        appData: this.dataSubject.value,
      }),
      catchError((error: string) => {
        this.notifier.onDefault(error);
        return of({ dataState: DataState.ERROR, error });
      })
    );
  }

  printReport() {
    window.print();
    //   let dataType = 'application/vnd.ms-excel.sheet.macroEnabled.12';
    //   let tableSelect = document.getElementById('servers');
    //   let tableHTML = tableSelect.outerHTML.replace(/ /g, '%20');
    //   let downloadLink = document.createElement('a');
    //   document.body.appendChild(downloadLink);
    //   downloadLink.href = 'data:' + dataType + ', ' + tableHTML;
    //   downloadLink.download = 'Server-Report.xls';
    //   downloadLink.click();
    //   document.body.removeChild(downloadLink);
  }
}
