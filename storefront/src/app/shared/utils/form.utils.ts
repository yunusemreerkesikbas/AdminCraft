import { FormGroup } from '@angular/forms';

export class FormUtils {
    /**
     * Maps backend validation errors to Angular form controls.
     * @param form The FormGroup to set errors on
     * @param errors Key-value map of field names and error messages (from backend "data" field)
     */
    static setServerErrors(
        form: FormGroup,
        errors: Record<string, string>
    ): void {
        if (!errors) return;

        Object.keys(errors).forEach((key) => {
            // Backend fields might be nested like 'global.whatsappPhone'.
            // Angular's form.get() handles dot notation correctly for nested controls.
            const control = form.get(key);
            if (control) {
                control.setErrors({ serverError: errors[key] });
                control.markAsTouched();
            } else {
                console.warn(
                    `Form control not found for server error field: ${key}`
                );
            }
        });
    }

    static getDirtyValues<T = any>(form: FormGroup): Partial<T> {
        const dirtyValues: any = {};

        Object.keys(form.controls).forEach(key => {
            const control = form.controls[key];

            if (control.dirty) {
                if (control instanceof FormGroup) {
                    const nested = FormUtils.getDirtyValues(control);
                    if (Object.keys(nested).length > 0) {
                        dirtyValues[key] = nested;
                    }
                } else {
                    dirtyValues[key] = control.value;
                }
            }
        });

        return dirtyValues;
    }
}
