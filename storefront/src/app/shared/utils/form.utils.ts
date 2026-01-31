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
}
