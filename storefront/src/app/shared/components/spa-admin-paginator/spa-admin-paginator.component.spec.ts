import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SpaAdminPaginatorComponent } from './spa-admin-paginator.component';

describe('SpaAdminPaginatorComponent', () => {
  let component: SpaAdminPaginatorComponent;
  let fixture: ComponentFixture<SpaAdminPaginatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SpaAdminPaginatorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SpaAdminPaginatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
