import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ComponentMetadataComponent } from 'app/shared/components/custom-ui/component-metadata/component-metadata.component';
import { I18nEditorComponent } from 'app/shared/components/custom-ui/i18n-editor/i18n-editor.component';
import { PanelListComponent } from 'app/shared/components/custom-ui/panel-list/panel-list.component';
import { take } from 'rxjs';
import { ComponentsService } from '../../components.service';
import { ComponentRequest, ComponentResponse } from '../../components.types';

@Component({
  selector: 'spa-navbar-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatSelectModule,
    PanelListComponent,
    ComponentMetadataComponent,
    I18nEditorComponent,
    TranslocoPipe,
  ],
  templateUrl: './navbar-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarFormComponent implements OnInit, OnDestroy {
  #svc = inject(ComponentsService);
  #tenant = inject(TenantContextService);
  #notify = inject(NotificationService);
  #i18n = inject(TranslocoService);
  #route = inject(ActivatedRoute);
  #router = inject(Router);
  #fb = inject(FormBuilder);

  form = this.#fb.group({
    key: ['', [Validators.required, Validators.pattern(/^[a-z0-9._-]+$/)]],
    visible: [true],
    status: ['ACTIVE' as 'ACTIVE' | 'INACTIVE'],
    sortOrder: [0],
    tr: this.#fb.group({
      title: [''],
      subtitle: [''],
      data: [''],
    }),
    en: this.#fb.group({
      title: [''],
      subtitle: [''],
      data: [''],
    }),
  });

  id: number | null = null;
  drawerMode: 'side' | 'over' = 'side';
  drawerOpened: boolean = true;
  selectedPanel: 'general' | 'tr' | 'en' = 'general';
  panels: Array<{ id: 'general' | 'tr' | 'en'; title: string; description: string; icon: string }> = [
    { id: 'general', title: 'General', description: 'Common settings', icon: 'heroicons_outline:adjustments-vertical' },
    { id: 'tr', title: 'Türkçe', description: 'TR içerik', icon: 'heroicons_outline:language' },
    { id: 'en', title: 'English', description: 'EN content', icon: 'heroicons_outline:language' }
  ];

  ngOnInit(): void {
    const idParam = this.#route.snapshot.paramMap.get('id');
    this.id = idParam ? Number(idParam) : null;
    if (this.id) {
      this.#load(this.id);
    }
  }

  ngOnDestroy(): void {}

  save(): void {
    const tenantId = this.#tenant.getCurrentTenantId();
    if (!tenantId) {
      this.#notify.alert('admin.common.messages.error');
      return;
    }
    const v = this.form.getRawValue();
    const payload: ComponentRequest = {
      tenantId,
      type: 'NAVBAR',
      key: v.key!,
      visible: !!v.visible,
      status: v.status!,
      sortOrder: v.sortOrder ?? 0,
      translations: {
        tr: v.tr as any,
        en: v.en as any,
      },
    };

    if (this.id) {
      this.#svc
        .updateByType(this.id, payload)
        .pipe(take(1))
        .subscribe({
          next: () => {
            this.#notify.success(this.#i18n.translate('admin.common.messages.updateSuccess', { item: this.#i18n.translate('admin.components.navbar.title') }));
            this.#router.navigate(['../'], { relativeTo: this.#route });
          },
          error: () => this.#notify.alert('admin.common.messages.error'),
        });
      return;
    }

    this.#svc
      .createByType(payload)
      .pipe(take(1))
      .subscribe({
        next: (res: ComponentResponse) => {
          this.#notify.success(this.#i18n.translate('admin.common.messages.createSuccess', { item: this.#i18n.translate('admin.components.navbar.title') }));
          this.#router.navigate(['../', res.id], { relativeTo: this.#route });
        },
        error: () => this.#notify.alert('admin.common.messages.error'),
      });
  }

  #load(id: number): void {
    this.#svc
      .getByType('NAVBAR', id)
      .pipe(take(1))
      .subscribe({
        next: (res) => {
          this.form.patchValue({
            key: res.key,
            visible: res.visible,
            status: res.status,
            sortOrder: res.sortOrder,
            tr: res.tr ?? {},
            en: res.en ?? {},
          });
        },
        error: () => this.#notify.alert('admin.common.messages.error'),
      });
  }

  getPanelInfo(id: 'general' | 'tr' | 'en') {
    const found = this.panels.find((p) => p.id === id);
    return found ?? this.panels[0];
  }

  goToPanel(id: 'general' | 'tr' | 'en'): void {
    this.selectedPanel = id;
  }
}


