
export type Language = 'TR' | 'EN' | 'ES' | 'RU' | 'AR';

export const AVAILABLE_LANGUAGES: readonly Language[] = ['TR', 'EN', 'ES', 'RU', 'AR'] as const;

export enum SlotPosition {
    TOP = 'TOP',
    CENTER = 'CENTER',
    BOTTOM = 'BOTTOM',
    LEFT = 'LEFT',
    RIGHT = 'RIGHT'
}

export const SLOT_POSITION_OPTIONS: { value: SlotPosition; label: string }[] = [
    { value: SlotPosition.TOP, label: 'Top' },
    { value: SlotPosition.CENTER, label: 'Center' },
    { value: SlotPosition.BOTTOM, label: 'Bottom' },
    { value: SlotPosition.LEFT, label: 'Left' },
    { value: SlotPosition.RIGHT, label: 'Right' }
];
