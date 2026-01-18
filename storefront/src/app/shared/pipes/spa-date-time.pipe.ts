import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'spaDateTime',
    standalone: true,
})
export class SpaDateTimePipe implements PipeTransform {
    transform(value: string | Date | null | undefined): string {
        if (!value) {
            return '-';
        }

        const date = value instanceof Date ? value : new Date(value);
        if (Number.isNaN(date.getTime())) {
            return '-';
        }

        const pad = (input: number): string => input.toString().padStart(2, '0');

        const day = pad(date.getDate());
        const month = pad(date.getMonth() + 1);
        const year = date.getFullYear();
        const hours = pad(date.getHours());
        const minutes = pad(date.getMinutes());

        return `${day}.${month}.${year} ${hours}:${minutes}`;
    }
}
