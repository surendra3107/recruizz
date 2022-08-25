import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'portalFilter',
  pure: false
})
export class MyFilterPipe implements PipeTransform {
  transform(items: any[], searchText: string): any[] {
    if (!items) return [];
    if (!searchText) return items;
    searchText = searchText.toLowerCase();
    return items.filter(it => {
      return it.source.toLowerCase().includes(searchText);
    });
  }
}
