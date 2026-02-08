# SpaTextareaComponent

Angular 19 textarea component with optional rich text editing via Quill.

## Features

- ✅ Standard textarea mode (default)
- ✅ Rich text editor mode with Quill (optional)
- ✅ Form validation integration
- ✅ ControlValueAccessor support
- ✅ Material Design styling
- ✅ Error messages with i18n
- ✅ OnPush change detection

## Usage

### Basic Textarea

```typescript
import { SpaTextareaComponent } from '@shared/components/custom-ui';

@Component({
  standalone: true,
  imports: [SpaTextareaComponent, ReactiveFormsModule],
  template: `
    <spa-textarea
      label="Description"
      placeholder="Enter description..."
      [control]="form.controls.description"
      [rows]="5"
    />
  `
})
export class MyComponent {
  protected form = new FormGroup({
    description: new FormControl('', [Validators.required, Validators.maxLength(500)])
  });
}
```

### Rich Text Editor Mode

```typescript
import { SpaTextareaComponent } from '@shared/components/custom-ui';

@Component({
  standalone: true,
  imports: [SpaTextareaComponent, ReactiveFormsModule],
  template: `
    <spa-textarea
      label="Content"
      placeholder="Write your content..."
      [control]="form.controls.content"
      [useRichText]="true"
      [rows]="10"
    />
  `
})
export class MyComponent {
  protected form = new FormGroup({
    content: new FormControl('', Validators.required)
  });
}
```

## Input Properties

| Property      | Type      | Default | Description                        |
| ------------- | --------- | ------- | ---------------------------------- |
| label         | string    | -       | Field label                        |
| labelTooltip  | string    | -       | Tooltip for label                  |
| placeholder   | string    | -       | Placeholder text                   |
| hint          | string    | -       | Hint text below field              |
| rows          | number    | 3       | Number of rows (textarea height)   |
| styleClasses  | string    | -       | Additional CSS classes             |
| showErrors    | boolean   | true    | Show validation errors             |
| **useRichText** | **boolean** | **false** | **Enable Quill rich text editor** |
| control       | FormControl | -     | Form control for validation        |

## Output Events

| Event       | Type                  | Description                    |
| ----------- | --------------------- | ------------------------------ |
| inputChange | EventEmitter<string>  | Emits on content change        |

## Rich Text Features

When `useRichText` is enabled, the following toolbar options are available:

- **Formatting**: Bold, Italic, Underline, Strike
- **Blocks**: Blockquote, Code block
- **Lists**: Ordered, Bullet
- **Headers**: H1-H6
- **Links**: Insert/edit links
- **Clean**: Remove formatting

## Validation

Supports Angular form validation with automatic error message display:

- `required` - Field is required
- `minlength` - Minimum length validation
- `maxlength` - Maximum length validation
- `serverError` - Server-side validation errors

## Styling

The component uses:
- Material Design form field (standard mode)
- Quill Snow theme (rich text mode)
- Custom SCSS for consistent look and feel

## Implementation Details

### TypeScript

```typescript
@Component({
  selector: 'spa-textarea',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    TranslocoModule,
    QuillModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaTextareaComponent implements ControlValueAccessor, AfterViewInit
```

### Quill Configuration

```typescript
protected quillModules = {
  toolbar: [
    ['bold', 'italic', 'underline', 'strike'],
    ['blockquote', 'code-block'],
    [{ list: 'ordered' }, { list: 'bullet' }],
    [{ header: [1, 2, 3, 4, 5, 6, false] }],
    ['link'],
    ['clean'],
  ],
};
```

## Dependencies

- `@angular/material` - Form field, input
- `ngx-quill` v27.0.0 - Rich text editor
- `quill` v2.0.3 - WYSIWYG editor core
- `@jsverse/transloco` - i18n support

## Notes

- Rich text content is stored as HTML
- Use `useRichText="false"` (default) for plain text
- Component handles both ngControl and control input binding
- Automatic change detection with OnPush strategy
- Error state matching integrated with Material form field
